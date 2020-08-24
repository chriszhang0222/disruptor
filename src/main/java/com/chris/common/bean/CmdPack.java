package com.chris.common.bean;

import com.chris.common.order.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmdPack implements Serializable {

    private long packNo;

    private List<OrderDto> orderCmds;
}
