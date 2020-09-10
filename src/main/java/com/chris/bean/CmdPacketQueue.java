package com.chris.bean;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.util.Bits;
import com.chris.common.bean.CmdPack;
import com.chris.common.codec.IBodyCodec;
import com.chris.common.order.OrderDto;
import com.chris.core.EngineApi;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CmdPacketQueue {

    private static volatile CmdPacketQueue instance = null;

    private final BlockingQueue<CmdPack> recvCache = new LinkedBlockingQueue<>();

    private CmdPacketQueue(){}

    public static CmdPacketQueue getInstance(){
        if(instance == null){
            synchronized (CmdPacketQueue.class){
                if(instance == null){
                    instance = new CmdPacketQueue();
                }
            }
        }
        return instance;
    }


    public void cache(CmdPack pack){
        recvCache.offer(pack);
    }

    private RheaKVStore rheaKVStore;

    private IBodyCodec codec;

    private EngineApi engineApi;

    public void init(RheaKVStore kvStore, IBodyCodec codec, EngineApi engineApi){
        this.rheaKVStore = kvStore;
        this.codec = codec;
        this.engineApi = engineApi;

        new Thread(() -> {
            while(true){
                try{
                    CmdPack cmds = recvCache.poll(10, TimeUnit.SECONDS);
                    if(cmds != null){
                        handle(cmds);
                    }
                }catch (Exception e){
                    log.error("msg packet recvcache error  ",e);
                }
            }
        }).start();
    }

    private long lastPackNo = -1;

    private void handle(CmdPack cmd) throws Exception{
        log.info("recv:{} in queue", cmd);
        //NACK 校验
        long packNo = cmd.getPackNo();
        if(packNo == lastPackNo + 1){
            if(CollectionUtils.isEmpty(cmd.getOrderCmds())){
                return;
            }
            for(OrderDto dto: cmd.getOrderCmds()){
                engineApi.submitCommand(dto);
            }
        }else if(packNo <= lastPackNo){
            //收到重复包
            log.warn("recv duplicate packid: {}", packNo);
        }else {
            log.info("packNo from {} to {}, begin query from sequence", lastPackNo + 1, packNo);
            //请求缺失数据
            byte[] firstKey = new byte[8];
            Bits.putLong(firstKey, 0, lastPackNo + 1);
            byte[] lastKey = new byte[8];
            Bits.putLong(lastKey, 0, packNo + 1);

            final List<KVEntry> kvEntryList = rheaKVStore.bScan(firstKey, lastKey);
            if (CollectionUtils.isNotEmpty(kvEntryList)) {
                List<CmdPack> collect = Lists.newArrayList();
                for (KVEntry entry : kvEntryList) {
                    byte[] value = entry.getValue();
                    if (ArrayUtils.isNotEmpty(value)) {
                        collect.add(codec.deserialize(value, CmdPack.class));
                    }
                }
                collect.sort((o1, o2) -> (int) (o1.getPackNo() - o2.getPackNo()));
                for (CmdPack pack : collect) {
                    if (CollectionUtils.isEmpty(pack.getOrderCmds())) {
                        continue;
                    }
                    for (OrderDto orderDto : pack.getOrderCmds()) {
                        engineApi.submitCommand(orderDto);
                    }
                }
            }

        }
        lastPackNo = packNo;
    }
}
