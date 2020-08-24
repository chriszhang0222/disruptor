package com.chris.common.codec;
import com.alipay.remoting.serialization.SerializerManager;

public class BodyCodec implements IBodyCodec {

    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        return SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(bytes, clazz.getName());
    }

    public static void main(String[] args) throws Exception{
        String a = "test";
        String b = "test1";
        String c = "test";
        BodyCodec bodyCodec = new BodyCodec();
        bodyCodec.serialize(a);
    }
}
