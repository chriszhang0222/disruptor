package com.chris.common.codec;

import com.chris.common.bean.CommonMsg;
import io.vertx.core.buffer.Buffer;

public interface IMsgCodec {

    Buffer encodeToBuffer(CommonMsg msg);

    CommonMsg decodeFromBuffer(Buffer buffer);
}
