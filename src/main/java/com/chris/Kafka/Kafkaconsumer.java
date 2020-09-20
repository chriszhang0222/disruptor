package com.chris.Kafka;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Log4j2
public class Kafkaconsumer {
    public static final String topic = "kafka-test";
    public static void main(String[] args) {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.0.21:9092");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        //自动提交间隔时间
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        //key反序列化
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerDeserializer");
        //value反序列化
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        //创建消费者
        KafkaConsumer<Integer,String> consumer = new KafkaConsumer<Integer,String>(properties);

        consumer.subscribe(Collections.singleton(topic));
        while(true){
            ConsumerRecords<Integer, String> records = consumer.poll(Duration.ofSeconds(100));
            for (ConsumerRecord<Integer, String> record : records) {
                System.out.println(record.key() + " " + record.value() + " -> offset:" + record.offset());
            }
        }
    }
}
