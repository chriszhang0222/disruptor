package com.chris.handler;

import com.lmax.disruptor.ExceptionHandler;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Log4j2
@AllArgsConstructor
public class DisruptorExceptionHandler<T> implements ExceptionHandler<T> {

    public final String name;
    public final BiConsumer<Throwable, Long> onException;

    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
        if(log.isDebugEnabled()){
            log.debug("Disruptor '{}' seq={} caught exception: {}", name, sequence, event, ex);
        }
        onException.accept(ex, sequence);
    }

    @Override
    public void handleOnStartException(Throwable throwable) {
        if (log.isDebugEnabled()) {
            log.debug("Disruptor '{}' startup exception: {}", name, throwable);
        }
    }

    @Override
    public void handleOnShutdownException(Throwable throwable) {
        if (log.isDebugEnabled()) {
            log.debug("Disruptor '{}' shutdown exception: {}", name, throwable);
        }
    }
}
