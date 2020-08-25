package com.chris;

import com.chris.bean.EngineConfig;
import com.chris.common.checksum.ByteCheckSum;
import com.chris.common.codec.BodyCodec;
import com.chris.common.codec.MsgCodec;

public class EngineStartup {

    public static void main(String[] args) throws Exception{
        EngineConfig engineConfig = new EngineConfig(
                "disruptor.properties",
                new BodyCodec(),
                new ByteCheckSum(),
                new MsgCodec()
        );
        engineConfig.startup();
    }
}
