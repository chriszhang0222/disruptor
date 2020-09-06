package com.chris.core;


import com.chris.bean.command.RbCmd;
import com.chris.common.order.CmdType;
import com.chris.common.order.OrderDto;
import com.chris.handler.BaseHandler;
import com.chris.handler.DisruptorExceptionHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import com.chris.bean.RbCmdFactory;

import java.util.Timer;
import java.util.TimerTask;

import static com.chris.handler.pub.PubHandler.HQ_PUB_RATE;


@Log4j2
public class EngineCore {

    private final Disruptor<RbCmd> disruptor;
    public static final int BUFFER_SIZE = 1024;

    @Getter
    private final EngineApi api;

    public EngineCore(
            @NonNull final BaseHandler riskHandler,
            @NonNull final BaseHandler matchHandler,
            @NonNull final BaseHandler pubHandler
    ) {
        this.disruptor = new Disruptor<>(
                new RbCmdFactory(),
                BUFFER_SIZE,
                new AffinityThreadFactory("aft_engine_core",AffinityStrategies.ANY),
                ProducerType.SINGLE,
                new BlockingWaitStrategy()
        );
        this.api = new EngineApi(disruptor.getRingBuffer());

        final DisruptorExceptionHandler<RbCmd> exceptionHandler =
                new DisruptorExceptionHandler<>("main", (ex, seq) -> {
                   log.error("exception thrown on seq={}", seq, ex);
                });
        disruptor.setDefaultExceptionHandler(exceptionHandler);
        disruptor.handleEventsWith(riskHandler)
                .then(matchHandler)
                .then(pubHandler);
        //启动
        disruptor.start();
        log.info("match engine start!");

        //4. 定时任务 发布行情
        new Timer().schedule(new HqPubTask(), 1000, HQ_PUB_RATE);

    }

    private class HqPubTask extends TimerTask{

        @Override
        public void run() {
            api.submitCommand(OrderDto.builder().type(CmdType.HQ_PUB).build());
        }
    }
}
