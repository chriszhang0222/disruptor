package com.chris.common.hq;

import com.chris.common.order.OrderStatus;
import lombok.Builder;

/*
* 用在总线上， 给柜台和其他终端使用， 对所有服务公开
* */
@Builder
public class MatchData {

    public long timestamp;

    public short mid;

    public long oid;

    public OrderStatus status;

    public long tid;

    public long volume;

    public long price;
}
