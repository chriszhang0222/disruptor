package com.chris.common.hq;

import com.chris.common.order.OrderStatus;
import lombok.Builder;

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
