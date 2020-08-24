package com.chris.common.fetch;

import com.chris.common.order.OrderDto;

import java.util.List;

public interface IFetchService {

    List<OrderDto> fetchData();
}
