package com.chris.common.bean;


import com.chris.common.order.OrderDto;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class OrderDtoContainerQueue {

    private static OrderDtoContainerQueue instance = new OrderDtoContainerQueue();
    private OrderDtoContainerQueue(){

    }

    public static OrderDtoContainerQueue getInstance(){
        return instance;
    }

    private final BlockingQueue<OrderDto> queue = new LinkedBlockingDeque<>();

    public boolean cache(OrderDto orderDto){
        return queue.offer(orderDto);
    }

    public int size(){
        return queue.size();
    }

    public List<OrderDto> getAll(){
        List<OrderDto> msgList = Lists.newArrayList();
        int count = queue.drainTo(msgList);
        if(count == 0)
            return null;
        return msgList;
    }
}
