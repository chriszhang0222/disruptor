package com.chris.common.bus;

import com.chris.common.bean.CommonMsg;
import com.chris.common.codec.IMsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;


@Log4j2
@RequiredArgsConstructor
public class MQTTBusSender implements IBusSender{

    @NonNull
    private String busIp;

    @NonNull
    private int busPort;

    @NonNull
    private IMsgCodec msgCodec;

    @NonNull
    private Vertx vertx;


    private volatile MqttClient mqttClient;
    @Override
    public void startUp() {
        //connect bus
        mqttConnect();
    }

    private void mqttConnect() {
        mqttClient = MqttClient.create(vertx);
        mqttClient.connect(busPort, busIp, res -> {
           if(res.succeeded()){
               log.info("Connected to Bus:{}-{}", busIp, busPort);
           }else{
               log.error("Connect to bus failed:{}-{}", busIp, busPort);
               mqttConnect();
           }
        });

        mqttClient.closeHandler(h -> {
           try{
               TimeUnit.SECONDS.sleep(5);
           }catch (Exception e){
               log.error(e);
           }
           mqttConnect();
        });
    }

    @Override
    public void publish(CommonMsg commonMsg) {
        mqttClient.publish(Short.toString(commonMsg.getMsgDst()), msgCodec.encodeToBuffer(commonMsg),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false);
    }
}
