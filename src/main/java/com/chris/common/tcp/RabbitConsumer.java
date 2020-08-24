package com.chris.common.tcp;

import com.alibaba.fastjson.JSON;
import com.chris.common.bean.CommonMsg;
import com.rabbitmq.client.*;

import java.io.IOException;

public class RabbitConsumer {
    public static Channel channel;

    public static Connection connection;
    public static Consumer consumer;
    private static final String queue_name = "ordercmd";
    private static final String exchangeName = "amq.fanout";
    private static final String routingKey = "counter_routing";

    public static void main(String[] args) throws Exception {
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
        consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("OK!");
                CommonMsg msg = (CommonMsg) JSON.parseObject(body, CommonMsg.class);
                int i = 0;
            }
        };
        channel.basicConsume(queue_name, consumer);
    }
}
