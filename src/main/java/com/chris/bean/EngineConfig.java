package com.chris.bean;

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.chris.common.checksum.ICheckSum;
import com.chris.common.codec.IBodyCodec;
import com.chris.common.codec.IMsgCodec;
import io.vertx.core.Vertx;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Properties;

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
    private IBodyCodec bodyCodec;

    @NonNull
    private ICheckSum cs;

    @NonNull
    private IMsgCodec msgCodec;

    private Vertx vertx = Vertx.vertx();

    @ToString.Exclude
    private final RheaKVStore orderkvStore = new DefaultRheaKVStore();

    public void startup() throws Exception{
        initConfig();
        initDB();
        startSeqConn();

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

    }

    private void initDB() {
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
