package com.chris.Kafka;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@Log4j2
public class Kafkaproducer {
    public static KafkaProducer<Integer, String> kafkaProducer;

    public static final String topic = "kafka-test";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.21:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        //value序列化
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //创建生产者
        KafkaProducer<Integer,String> producer = new KafkaProducer<Integer,String>(properties);
        try {
            producer.send(new ProducerRecord<Integer, String>(topic, "hello-kafka")).get();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
