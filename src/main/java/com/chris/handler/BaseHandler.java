package com.chris.handler;

import com.chris.bean.command.RbCmd;
import com.lmax.disruptor.EventHandler;

public abstract class BaseHandler implements EventHandler<RbCmd> {

}
