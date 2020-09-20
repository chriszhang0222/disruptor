package com.chris.bean;

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.chris.bean.orderbook.IOrderBook;
import com.chris.bean.orderbook.OrderBookImpl;
import com.chris.common.bean.CmdPack;
import com.chris.common.bus.IBusSender;
import com.chris.common.bus.MQTTBusSender;
import com.chris.common.checksum.ICheckSum;
import com.chris.common.codec.IBodyCodec;
import com.chris.common.codec.IMsgCodec;
import com.chris.common.hq.MatchData;
import com.chris.core.EngineApi;
import com.chris.core.EngineCore;
import com.chris.db.DbQuery;
import com.chris.handler.BaseHandler;
import com.chris.handler.pub.PubHandler;
import com.chris.handler.risk.ExistRiskHandler;
import com.chris.handler.stock.StockHandler;
import com.google.common.collect.Lists;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.protostuff.runtime.ArraySchemas;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.dbutils.QueryRunner;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.*;
import java.util.zip.ZipFile;

@Log4j2
@Data
@RequiredArgsConstructor
public class EngineConfig {

    private short id;
    private String orderRecvip;
    private Integer orderRecvPort;

    private String seqUrlList;
    private String pubIp;
    private int pubPort;

    @NonNull
    private String fileName;

    @NonNull
    @ToString.Exclude
    private IBodyCodec bodyCodec;

    @NonNull
    @ToString.Exclude
    private ICheckSum cs;

    @NonNull
    @ToString.Exclude
    private IMsgCodec msgCodec;

    @ToString.Exclude
    private Vertx vertx = Vertx.vertx();

    @Getter
    @ToString.Exclude
    private EngineApi engineApi;

    @ToString.Exclude
    private final RheaKVStore orderkvStore = new DefaultRheaKVStore();

    @ToString.Exclude
    private IBusSender busSender;

    public void startup() throws Exception{
        initConfig();
        initDB();
        startEngine();

        //初始化发布总线
        initPub();

        //连接排队机, 接收从排队机到达的udp 数据包
        startSeqConn();

    }

    private void initPub() {
        busSender = new MQTTBusSender(pubIp, pubPort, msgCodec, vertx);
        busSender.startUp();
    }

    private void startEngine() throws Exception{

        //1.前置风控处理器
        final BaseHandler riskHandler = new ExistRiskHandler(
                dbQuery.queryAllBalance().keySet(),
                dbQuery.queryAllStockCode()
        );
        //2. 撮合处理器
        IntObjectHashMap<IOrderBook> orderBookMap = new IntObjectHashMap<>();
        dbQuery.queryAllStockCode().forEach(code -> orderBookMap.put(code, new OrderBookImpl(code)));
        final BaseHandler matchHandler = new StockHandler(orderBookMap);
        //3. 发布处理器

        ShortObjectHashMap<List<MatchData>> matcherEventMap = new ShortObjectHashMap<>();
        for(short id: dbQuery.queryAllMemberIds()){
            matcherEventMap.put(id, Lists.newArrayList());
        }
        final BaseHandler pubHandler = new PubHandler(matcherEventMap,this);
        engineApi = new EngineCore(
                riskHandler,
                matchHandler,
                pubHandler
        ).getApi();
    }

    private void startSeqConn() throws Exception{
        final List<RegionRouteTableOptions> regionRouteTableOptions =
                MultiRegionRouteTableOptionsConfigured.newConfigured()
                .withInitialServerList(-1L, seqUrlList)
                .config();
        final PlacementDriverOptions pdOptions = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true)
                .withRegionRouteTableOptionsList(regionRouteTableOptions)
                .config();
        final RheaKVStoreOptions opts = RheaKVStoreOptionsConfigured.newConfigured()
                .withPlacementDriverOptions(pdOptions)
                .config();

        orderkvStore.init(opts);

        CmdPacketQueue.getInstance().init(orderkvStore, bodyCodec, engineApi);
        //接收udp数据, 组播，多个udp终端接收同一个udp包
        DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());
        socket.listen(orderRecvPort, "0.0.0.0", asyncRes ->{
            if(asyncRes.succeeded()){
                socket.handler(packet -> {
                   Buffer udpData = packet.data();
                   if(udpData.length() > 0){
                       try {
                           log.info("received data packet from udp port:{}", orderRecvPort);
                           CmdPack pack = bodyCodec.deserialize(udpData.getBytes(), CmdPack.class);
                           CmdPacketQueue.getInstance().cache(pack);
                       }catch (Exception e){
                           log.error("decode packet error", e);
                       }
                   }else{
                       log.error("recv empty udp packet from client: {}", packet.sender().toString());
                   }
                });
                try{
                    socket.listenMulticastGroup(orderRecvip, mainInterface().getName(), null, asyncRes2 -> {
                        log.info("listen success {}", asyncRes2.succeeded());
                    });
                }catch (Exception e){
                    log.error(e);
                }
            }else{
                log.error("Listen failed,", asyncRes.cause());
            }
        });

    }

    private static NetworkInterface mainInterface() throws Exception{
        final ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        final NetworkInterface networkInterface = interfaces.stream().filter(
                t -> {
                    try{
                        final boolean isloopback = t.isLoopback();
                        final boolean supportMulticast = t.supportsMulticast();
                        final boolean isVirtualBox = t.getDisplayName().contains("VirtualBox")
                                || t.getDisplayName().contains("Host-only");
                        final boolean hasIpv4 = t.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
                        return !isloopback & supportMulticast & !isVirtualBox & hasIpv4;
                    }catch (Exception e){
                        log.error(e);
                    }
                    return false;
                }
        ).sorted(Comparator.comparing(NetworkInterface::getName)).findFirst().orElse(null);
        return networkInterface;
    }

    @Getter
    @ToString.Exclude
    private DbQuery dbQuery;

    private void initDB() {
        QueryRunner runner = new QueryRunner(new ComboPooledDataSource());
        dbQuery = new DbQuery(runner);
    }

    private void initConfig() throws Exception{
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/" + fileName));

        id = Short.parseShort(properties.getProperty("id"));
        orderRecvip = properties.getProperty("orderrecvip");
        orderRecvPort = Integer.parseInt(properties.getProperty("orderrecvport"));
        seqUrlList = properties.getProperty("sequrllist");
        pubIp = properties.getProperty("pubip");
        pubPort = Integer.parseInt(properties.getProperty("pubport"));
        log.info("completed init config");
    }
}
