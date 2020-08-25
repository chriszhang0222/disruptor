package com.chris.bean.orderbook;

import com.chris.common.hq.MatchData;
import com.chris.common.order.OrderStatus;
import lombok.NoArgsConstructor;
import lombok.ToString;
/*
* Disruptor内部使用  (不同于MatchData）
* */
@NoArgsConstructor
@ToString
public class MatchEvent {
    public long timestamp;

    /**
     * 会员ID
     */
    public short mid;

    /**
     * 委托编号
     */
    public long oid;

    public OrderStatus status = OrderStatus.NOT_SET;

    public long tid;

    //撤单数量 成交数量
    public long volume;

    public long price;


    public MatchData copy() {
        return MatchData.builder()
                .timestamp(this.timestamp)
                .mid(this.mid)
                .oid(this.oid)
                .status(this.status)
                .tid(this.tid)
                .volume(this.volume)
                .price(this.price)
                .build();

    }
}
