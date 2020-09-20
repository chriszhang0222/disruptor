package com.chris.bean.command;

import com.chris.bean.orderbook.MatchEvent;
import com.chris.common.hq.L1MarketData;
import com.chris.common.order.CmdType;
import com.chris.common.order.OrderDirection;
import com.chris.common.order.OrderType;
import lombok.Builder;
import lombok.ToString;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.List;

@Builder
@ToString
public class RbCmd {

    public long timestamp;

    //member id
    public short mid;

    //user id
    public long uid;

    public CmdType command;

    //stock code
    public int code;

    public OrderDirection direction;

    public long price;

    public long volume;

    public long oid;

    public OrderType orderType;

    // 保存撮合结果
    public List<MatchEvent> matchEventList;

    // 前置风控 --> 撮合 --> 发布
    public CmdResultCode resultCode;

    // 保存行情
    public IntObjectHashMap<L1MarketData> marketDataMap;

}
