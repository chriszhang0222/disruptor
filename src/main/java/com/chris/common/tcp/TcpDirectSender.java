package com.chris.common.tcp;

import com.alibaba.fastjson.JSON;
import com.chris.common.bean.CommonMsg;
import com.chris.common.codec.IMsgCodec;
import com.chris.common.codec.MsgCodec;
import com.rabbitmq.client.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class TcpDirectSender {

    @NonNull
    private String ip;

    @NonNull
    private int port;

    @NonNull
    private Vertx vertx;

    private volatile NetSocket socket;

    private Channel channel;

    private Connection connection;
    private Consumer consumer;
    private IMsgCodec msgCodec = new MsgCodec();

    private static final String queue_name = "ordercmd";
    private static final String exchangeName = "amq.fanout";
    private static final String routingKey = "counter_routing";


    public TcpDirectSender(@NonNull String ip, @NonNull int port, @NonNull Vertx vertx) {
        this.ip = ip;
        this.port = port;
        this.vertx = vertx;
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("127.0.0.1");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setVirtualHost("/");
            connection = factory.newConnection();

            channel = connection.createChannel();
            channel.exchangeDeclare("amq.fanout", "fanout", true);
            channel.queueDeclare(queue_name, true, false, true, null);
            channel.queueBind(queue_name, exchangeName, routingKey);
            log.info("connect to rabbitMQ:{}", 5672);

        }catch (Exception e){
            log.error("error when init rabbitMQ conn",e);
        }
    }

    private class ClientConnHandler implements Handler<AsyncResult<NetSocket>>{

        private void reconnect(){
            vertx.setTimer(1000 * 5, r->{
               log.info("try reconnect to server to {}:{} failed", ip, port);
            });
            vertx.createNetClient()
                    .connect(port, ip, new ClientConnHandler());
        }

        @Override
        public void handle(AsyncResult<NetSocket> result) {
            if(result.succeeded()){
                log.info("connect success to remote {}:{}", ip, port);
                socket = result.result();

                socket.closeHandler(close -> {
                    log.info("connect to remote {} closed", socket.remoteAddress());
                    reconnect();
                });

                socket.exceptionHandler(ex -> {
                    log.error("error when connect to remote", ex.getCause());
                });
            }
        }
    }

    private final BlockingQueue<Buffer> sendCache = new LinkedBlockingQueue<>();

    public void startup(){
        vertx.createNetClient().connect(port, ip, new ClientConnHandler());
        consumer = new DefaultConsumer(channel){
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException{
                        CommonMsg msg = (CommonMsg)JSON.parseObject(new String(body), CommonMsg.class);
                        Buffer buffer = msgCodec.encodeToBuffer(msg);
                        if(buffer != null && buffer.length() > 0 && socket != null){
                            socket.write(buffer);
                        }
                    }
        };
        try {
            channel.basicConsume(queue_name, consumer);
        }catch (Exception e){
            log.error("error when receiving message from Rabbit", e);
        }


//        new Thread(() -> {
//            while(true){
//                try{
//                    Buffer buffer = sendCache.poll(5, TimeUnit.SECONDS);
//                    if(buffer != null && buffer.length() > 0 && socket != null){
//                        socket.write(buffer);
//                    }
//                }catch (Exception e){
//                    log.error("msg send fali", e);
//                }
//            }
//        }).start();

    }

    public boolean send(CommonMsg msg){
        try {
            channel.basicPublish(exchangeName, routingKey, null, JSON.toJSONBytes(msg));
        }catch (IOException e){
            log.error("error when sending message", e);
        }
        return true;
    }
}
