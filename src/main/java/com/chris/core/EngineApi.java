package com.chris.core;

import com.chris.bean.command.CmdResultCode;
import com.chris.bean.command.RbCmd;
import com.chris.common.order.CmdType;
import com.chris.common.order.OrderDto;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class EngineApi {

    private final RingBuffer<RbCmd> ringBuffer;

    public void submitCommand(OrderDto dto){
        switch (dto.type){
            case HQ_PUB:
                ringBuffer.publishEvent(HQ_PUB_TRANSLATOR,dto);
                break;
            case NEW_ORDER:
                ringBuffer.publishEvent(NEW_ORDER_TRANSLATOR,dto);
                break;
            case CANCEL_ORDER:
                ringBuffer.publishEvent(CANCEL_ORDER_TRANSLATOR,dto);
                break;
            default:
                throw  new IllegalArgumentException("Unsupported dtp type: " + dto.getClass().getSimpleName());
        }
    }


    private static final EventTranslatorOneArg<RbCmd, OrderDto> NEW_ORDER_TRANSLATOR = (rbCmd, seq, newOrder) -> {
        rbCmd.command = CmdType.NEW_ORDER;
        rbCmd.timestamp = newOrder.timestamp;
        rbCmd.mid = newOrder.mid;
        rbCmd.uid = newOrder.uid;
        rbCmd.code = newOrder.code;
        rbCmd.direction = newOrder.direction;
        rbCmd.price = newOrder.price;
        rbCmd.volume = newOrder.volume;
        rbCmd.orderType = newOrder.orderType;
        rbCmd.oid = newOrder.oid;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };

    /**
     * 撤单trans
     */
    private static final EventTranslatorOneArg<RbCmd, OrderDto> CANCEL_ORDER_TRANSLATOR = (rbCmd, seq, cancelOrder) -> {
        rbCmd.command = CmdType.CANCEL_ORDER;
        rbCmd.timestamp = cancelOrder.timestamp;
        rbCmd.mid = cancelOrder.mid;
        rbCmd.uid = cancelOrder.uid;
        rbCmd.code = cancelOrder.code;
        rbCmd.oid = cancelOrder.oid;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };

    /**
     * 行情发送
     */
    private static final EventTranslatorOneArg<RbCmd, OrderDto> HQ_PUB_TRANSLATOR = (rbCmd, seq, hqPub) -> {
        rbCmd.command = CmdType.HQ_PUB;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };
}
