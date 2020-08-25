package com.chris.bean.orderbook;

import com.chris.bean.command.CmdResultCode;
import com.chris.bean.command.RbCmd;
import com.chris.common.hq.L1MarketData;


import static com.chris.common.hq.L1MarketData.L1_SIZE;

public interface IOrderBook {

    //1.新增委托
    //2.撤单
    //3.查询行情快照

    CmdResultCode newOrder(RbCmd cmd);

    CmdResultCode cancelOrder(RbCmd cmd);

    default L1MarketData getMarketDataSnapShot(){
        final int buySize = limitBuyBucketSize(L1_SIZE);
        final int sellSize = limitSellBucketSize(L1_SIZE);
        final L1MarketData data = new L1MarketData(buySize, sellSize);
        fillBuys(buySize, data);
        fillSells(sellSize, data);
        fillCode(data);
        data.timestamp = System.currentTimeMillis();
        return data;
    }

    int limitSellBucketSize(int l1Size);

    void fillCode(L1MarketData data);

    void fillSells(int sellSize, L1MarketData data);

    void fillBuys(int buySize, L1MarketData data);

    int limitBuyBucketSize(int l1Size);

}
