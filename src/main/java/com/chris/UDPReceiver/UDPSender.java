package com.chris.UDPReceiver;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
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

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());
        Buffer buffer = Buffer.buffer("hello");
// Send a Buffer
        socket.send(buffer, 1234, "230.0.0.1", asyncResult -> {
            System.out.println("Send succeeded? " + asyncResult.succeeded());
        });
// Send a String
        socket.send("A string used as content", 1234, "230.0.0.1", asyncResult -> {
            System.out.println("Send succeeded? " + asyncResult.succeeded());
        });
    }
}
