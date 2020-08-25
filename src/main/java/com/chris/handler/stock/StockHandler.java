package com.chris.handler.stock;

import com.chris.bean.command.CmdResultCode;
import com.chris.bean.command.RbCmd;
import com.chris.bean.orderbook.IOrderBook;
import com.chris.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

@RequiredArgsConstructor
@Log4j2
public class StockHandler extends BaseHandler{

    @NonNull
    private final IntObjectHashMap<IOrderBook> orderBookMap;


    @Override
    public void onEvent(RbCmd cmd, long seq, boolean endOfbatch) throws Exception {
        if(cmd.resultCode.getCode() < 0){
            log.warn("cmd:{} didn't pass risk handler", cmd.oid);
        }
        cmd.resultCode = processCmd(cmd);
    }

    private CmdResultCode processCmd(RbCmd cmd) {
        switch (cmd.command){
            case NEW_ORDER:
                return orderBookMap.get(cmd.code).newOrder(cmd);
            case CANCEL_ORDER:
                return orderBookMap.get(cmd.code).cancelOrder(cmd);
            case HQ_PUB:
                orderBookMap.forEachKeyValue((code, orderBook) -> {
                    cmd.marketDataMap.put(code,orderBook.getMarketDataSnapShot());
                });
            default:
                return CmdResultCode.SUCCESS;
        }
    }
}
