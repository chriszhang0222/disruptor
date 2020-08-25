package com.chris.bean.orderbook;

import com.chris.bean.command.RbCmd;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public interface IOrderBucket extends Comparable<IOrderBucket>{

    AtomicLong tidGen = new AtomicLong(0);
    //1.新增订单
    void put(Order order);

    //2.移除订单
    Order remove(long oid);

    //3.match
    long match(long volumeLeft, RbCmd cmd, Consumer<Order> removeOrderCallback);

    //4.行情发布
    long getPrice();
    void setPrice(long price);

    long getTotalVolume();

    @Override
    default int compareTo(@NotNull IOrderBucket o) {
        return Long.compare(this.getPrice(), o.getPrice());
    }

    static IOrderBucket create(OrderBucketImplType type){
        switch (type){
            case CHRIS:
                return new OrderBucketImpl();
            default:
                throw new IllegalArgumentException();
        }
    }

    enum OrderBucketImplType{
        CHRIS(0);

        private byte code;

        OrderBucketImplType(int code) {
            this.code = (byte)code;
        }
    }
}
