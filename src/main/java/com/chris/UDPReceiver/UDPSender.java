package com.chris.UDPReceiver;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UDPSender {
//    public static void main(String[] args) {
//        Vertx vertx = Vertx.vertx();
//        DatagramSocket datagramSocket = vertx.createDatagramSocket(new DatagramSocketOptions());
//        String message = "Hello Vertx";
//        Buffer buffer = Buffer.buffer(message);
//        datagramSocket.send(buffer, 1234, "230.0.0.1", res->{
//            if(res.succeeded()){
//                log.info("OK");
//            }
//        });
//    }

    public static void main(String[] args) throws Exception{
        Vertx vertx = Vertx.vertx();
        DatagramSocket datagramSocket = vertx.createDatagramSocket(new DatagramSocketOptions());
        int num = 0;
        String message = "data:{" + num + "}";
        while(true) {
            int finalNum = num;
            datagramSocket.send(message, 1234, "230.0.0.1", res -> {
                if (res.succeeded()) {
                    log.info("OK:{}", finalNum);
                }
            });
            num ++;
            Thread.sleep(4000);
            message = "data:{" + num + "}";
        }


    }

    @RequiredArgsConstructor
    public static class SendTask extends Thread{
        @NonNull
        private DatagramSocket socket;

        @NonNull
        private String buffer;

        @Override
        public void run() {
            while(true) {
                try {
                    socket.send(buffer, 1234, "230.0.0.1", async -> {
                        log.info("Succeed, {}", Thread.currentThread().getName());
                    });
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            }
        }
    }

}
