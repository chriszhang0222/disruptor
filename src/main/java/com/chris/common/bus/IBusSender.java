package com.chris.common.bus;

import com.chris.common.bean.CommonMsg;

public interface IBusSender {

    void startUp();

    void publish(CommonMsg commonMsg);
}
