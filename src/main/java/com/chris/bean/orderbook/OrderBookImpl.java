package com.chris.bean.orderbook;

import com.chris.bean.command.CmdResultCode;
import com.chris.bean.command.RbCmd;
import com.chris.common.hq.L1MarketData;
import com.chris.common.order.OrderDirection;
import com.chris.common.order.OrderStatus;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.util.*;

@RequiredArgsConstructor
public class OrderBookImpl implements IOrderBook{


    @NonNull
    private int code;

    //key price
    private final NavigableMap<Long, IOrderBucket> sellBuckets = new TreeMap<>();
    private final NavigableMap<Long, IOrderBucket> buyBuckets = new TreeMap<>(Collections.reverseOrder());

    private final LongObjectHashMap<Order> oidMap = new LongObjectHashMap<>();


    @Override
    public void fillCode(L1MarketData data) {
        data.code = code;
    }

    @Override
    public void fillSells(int size, L1MarketData data) {
        if(size == 0){
            return;
        }
        int i = 0;
        for(IOrderBucket bucket: sellBuckets.values()){
            data.sellPrices[i] = bucket.getPrice();
            data.sellVolumes[i] = bucket.getTotalVolume();
            if( ++i == size){
                break;
            }
        }
        data.sellSize = i;
    }

    @Override
    public void fillBuys(int size, L1MarketData data) {
        if(size == 0){
            return;
        }
        int i = 0;
        for(IOrderBucket bucket: buyBuckets.values()){
            data.buyPrices[i] = bucket.getPrice();
            data.buyVolumes[i] = bucket.getTotalVolume();
            if(++i == size){
                break;
            }
        }
        data.buySize = i;

    }

    @Override
    public int limitBuyBucketSize(int l1Size) {
        return Math.min(l1Size, buyBuckets.size());
    }

    @Override
    public int limitSellBucketSize(int l1Size) {
        return Math.min(l1Size, sellBuckets.size());
    }



    @Override
    public CmdResultCode newOrder(RbCmd cmd) {
        if(oidMap.containsKey(cmd.oid)){
            return CmdResultCode.DUPLICATE_ORDER_ID;
        }

        //2.生成order
        //2.1 预处理
        // 50 100  买单buckets 》=50
        NavigableMap<Long, IOrderBucket> matchBuckets =
                (cmd.direction == OrderDirection.SELL ? sellBuckets : buyBuckets)
                .headMap(cmd.price, true);
        long tVolume = preMatch(cmd, matchBuckets);
        if(tVolume == cmd.volume){
            return CmdResultCode.SUCCESS;
        }
        final Order order = Order.builder()
                .mid(cmd.mid)
                .uid(cmd.uid)
                .code(cmd.code)
                .direction(cmd.direction)
                .price(cmd.price)
                .volume(cmd.volume)
                .tvolume(tVolume)
                .oid(cmd.oid)
                .timestamp(cmd.timestamp)
                .build();
        if(tVolume == 0){
            genMatchEvent(cmd, OrderStatus.ORDER_ED);
        }else{
            genMatchEvent(cmd, OrderStatus.PART_TRADE);
        }
        final IOrderBucket bucket = (cmd.direction == OrderDirection.SELL ? sellBuckets : buyBuckets)
        .computeIfAbsent(cmd.price, p->{
            final IOrderBucket b = IOrderBucket.create(IOrderBucket.OrderBucketImplType.CHRIS);
            b.setPrice(p);
            return b;
        });
        bucket.put(order);
        oidMap.put(cmd.oid, order);
        return CmdResultCode.SUCCESS;
    }

    private void genMatchEvent(RbCmd cmd, OrderStatus status) {
        long now = System.currentTimeMillis();
        MatchEvent event = new MatchEvent();
        event.timestamp = now;
        event.mid = cmd.mid;
        event.oid = cmd.oid;
        event.status = status;
        event.volume = 0;
        cmd.matchEventList.add(event);

    }

    private long preMatch(RbCmd cmd, SortedMap<Long, IOrderBucket> matchBuckets) {
        int tVol = 0;
        if(matchBuckets.size() == 0){
            return tVol;
        }
        List<Long> emptyBuckets = Lists.newArrayList();
        for(IOrderBucket bucket: matchBuckets.values()){
           tVol += bucket.match(cmd.volume-tVol, cmd, order -> {
                oidMap.remove(order.getOid());
            });
           if(bucket.getTotalVolume() == 0){
                emptyBuckets.add(bucket.getPrice());
           }
           if(tVol == cmd.volume){
               break;
           }
        }
        emptyBuckets.forEach(matchBuckets::remove);
        return tVol;
    }

    @Override
    public CmdResultCode cancelOrder(RbCmd cmd) {

        Order order = oidMap.get(cmd.oid);
        if(order == null){
            return CmdResultCode.INVALID_ORDER_ID;
        }
        oidMap.remove(order.getOid());
        final NavigableMap<Long, IOrderBucket> buckets =
                order.getDirection() == OrderDirection.SELL ? sellBuckets : buyBuckets;
        IOrderBucket orderBucket = buckets.get(order.getPrice());
        orderBucket.remove(order.getOid());

        if(orderBucket.getTotalVolume() == 0){
            buckets.remove(order.getPrice());
        }
        MatchEvent cancelEvent = new MatchEvent();
        cancelEvent.timestamp = System.currentTimeMillis();
        cancelEvent.mid = order.getMid();
        cancelEvent.oid = order.getOid();
        cancelEvent.status = order.getTvolume() == 0 ? OrderStatus.CANCEL_ED : OrderStatus.PART_CANCEL;
        cancelEvent.volume = order.getTvolume() - order.getVolume();
        cmd.matchEventList.add(cancelEvent);

        return CmdResultCode.SUCCESS;
    }
}
