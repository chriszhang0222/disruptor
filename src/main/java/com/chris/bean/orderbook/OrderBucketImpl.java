package com.chris.bean.orderbook;

import com.chris.bean.command.RbCmd;
import com.chris.common.order.OrderStatus;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
@ToString
@Data
public class OrderBucketImpl implements IOrderBucket{

    //1.价格
    //2.量
    //3. 委托列表

    private long price;

    private long totalVolume = 0;

    private final LinkedHashMap<Long, Order> entries = new LinkedHashMap<>();


    @Override
    public void put(Order order) {
        entries.put(order.getOid(), order);
        totalVolume += order.getVolume() - order.getTvolume();
    }

    @Override
    public Order remove(long oid) {
        Order order = entries.get(oid);
        if(order == null){
            return null;
        }
        entries.remove(oid);
        totalVolume -= order.getVolume() - order.getTvolume();
        return order;
    }

    @Override
    public long match(long volumeLeft, RbCmd cmd, Consumer<Order> removeOrderCallback) {
        Iterator<Map.Entry<Long, Order>> iterator = entries.entrySet().iterator();
        long volumeMatch = 0;
        while(iterator.hasNext() && volumeLeft > 0){
            Map.Entry<Long, Order> next = iterator.next();
            Order order = next.getValue();

            //计算order 可以吃多少量
            long traded = Math.min(volumeLeft, order.getVolume() - order.getTvolume());
            volumeMatch += traded;

            //
            volumeLeft -= traded;
            order.setTvolume(order.getTvolume() + traded);
            totalVolume -= traded;

            //生成成交事件
            boolean fullMatch = order.getVolume() == order.getTvolume();
            genMatchEvent(order,cmd, fullMatch, volumeLeft == 0, traded);
            if(fullMatch){
                removeOrderCallback.accept(order);
                iterator.remove();
            }
        }
        return volumeMatch;
    }

    private void genMatchEvent(final Order order,final RbCmd cmd, boolean fullMatch, boolean cmdFullmatch, long traded) {
        //两个MatchEvent
        long tid = IOrderBucket.tidGen.getAndIncrement();
        long now = System.currentTimeMillis();
        MatchEvent bidEvent = new MatchEvent();
        bidEvent.timestamp = now;
        bidEvent.mid = cmd.mid;
        bidEvent.oid = cmd.oid;
        bidEvent.status = cmdFullmatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;
        bidEvent.volume = traded;
        bidEvent.price = order.getPrice();
        bidEvent.tid = tid;

        cmd.matchEventList.add(bidEvent);

        MatchEvent offerEvent = new MatchEvent();
        offerEvent.timestamp = now;
        offerEvent.mid = order.getMid();
        offerEvent.oid = order.getOid();
        offerEvent.status = fullMatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;
        offerEvent.price = order.getPrice();
        offerEvent.volume = traded;
        offerEvent.tid = tid;
        cmd.matchEventList.add(offerEvent);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        OrderBucketImpl that = (OrderBucketImpl) o;

        return new EqualsBuilder()
                .append(price, that.price)
                .append(entries, that.entries)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(price)
                .append(entries)
                .toHashCode();
    }


}
