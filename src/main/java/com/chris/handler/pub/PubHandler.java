package com.chris.handler.pub;

import com.chris.bean.EngineConfig;
import com.chris.bean.command.RbCmd;
import com.chris.bean.orderbook.MatchEvent;
import com.chris.common.bean.CommonMsg;
import com.chris.common.hq.L1MarketData;
import com.chris.common.hq.MatchData;
import com.chris.common.order.CmdType;
import com.chris.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.api.tuple.primitive.ShortObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;

import java.util.List;

import static com.chris.common.bean.MsgConstants.*;

@RequiredArgsConstructor
@Log4j2
public class PubHandler extends BaseHandler {

    @NonNull
    private final ShortObjectHashMap<List<MatchData>> matchEventMap;

    @NonNull
    private EngineConfig config;

    public static final int HQ_PUB_RATE = 5000;

    public static final short HQ_ADDRESS = -1;

    @Override
    public void onEvent(RbCmd cmd, long sequence, boolean endofbatch) throws Exception {
            final CmdType cmdType = cmd.command;
            if(cmdType == CmdType.NEW_ORDER || cmdType == CmdType.CANCEL_ORDER){
                for(MatchEvent e: cmd.matchEventList){
                    matchEventMap.get(e.mid).add(e.copy());
                }
            }else if(cmdType == CmdType.HQ_PUB){
                // 1.五档行情
                pubMarketData(cmd.marketDataMap);
                // 2.某柜台单独消息
                pubMatherData();

            }
    }

    private void pubMatherData() {
        if(matchEventMap.size() == 0){
            return;
        }
        log.info(matchEventMap);
        try{
            for(ShortObjectPair<List<MatchData>> s: matchEventMap.keyValuesView()){
                if(CollectionUtils.isEmpty(s.getTwo())){
                    continue;
                }
                byte[] serialize = config.getBodyCodec().serialize(s.getTwo().toArray(new MatchData[0]));
                pubData(serialize, s.getOne(), MATCH_ORDER_DATA);

                s.getTwo().clear();
            }
        }catch (Exception e){
            log.error(e);
        }
    }

    private void pubMarketData(IntObjectHashMap<L1MarketData> marketDataMap) {
        log.info(marketDataMap);
        byte[] serialize = null;
        try{
            serialize = config.getBodyCodec().serialize(marketDataMap.values().toArray(new L1MarketData[0]));
        }catch (Exception e){
            log.error(e);
        }
        if(serialize == null)
            return;
        pubData(serialize, HQ_ADDRESS, MATCH_HQ_DATA);
    }

    private void pubData(byte[] serialize, short dst, short msgType) {
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(serialize.length);
        msg.setChecksum(config.getCs().getCheckSum(serialize));
        msg.setMsgSrc(config.getId());
        msg.setMsgDst(dst);
        msg.setMsgType(msgType);
        msg.setStatus(NORMAL);
        msg.setBody(serialize);
        config.getBusSender().publish(msg);
    }
}
