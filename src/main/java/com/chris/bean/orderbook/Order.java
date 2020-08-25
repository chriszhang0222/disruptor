package com.chris.bean.orderbook;

import com.chris.common.order.OrderDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Order {

    //member id;
    private short mid;

    //user id;
    private long uid;

    //委托编号
    private long oid;

    private int code;

    private OrderDirection direction;

    private long price;

    private long volume;

    //已成交量
    private long tvolume;

    private long timestamp;

    private long innerOid;

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(mid)
                .append(uid)
                .append(code)
                .append(direction)
                .append(price)
                .append(volume)
                .append(tvolume)
                .append(oid)
//                .append(timestamp)
                .toHashCode();
    }

    /**
     * timestamp is not included into hashCode() and equals() for repeatable results
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return new EqualsBuilder()
                .append(mid, order.mid)
                .append(uid, order.uid)
                .append(code, order.code)
                .append(price, order.price)
                .append(volume, order.volume)
                .append(tvolume, order.tvolume)
                .append(oid, order.oid)
//                .append(timestamp, order.timestamp)
                .append(direction, order.direction)
                .isEquals();
    }


}
