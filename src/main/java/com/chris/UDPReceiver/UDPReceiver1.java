package com.chris.UDPReceiver;

import com.alibaba.fastjson.JSON;
import com.chris.common.codec.BodyCodec;
import com.chris.common.codec.IBodyCodec;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.extern.log4j.Log4j2;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@Log4j2
public class UDPReceiver1 {
    private static NetworkInterface mainInterface() throws Exception{
        final ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        final NetworkInterface networkInterface = interfaces.stream().filter(
                t -> {
                    try{
                        final boolean isloopback = t.isLoopback();
                        final boolean supportMulticast = t.supportsMulticast();
                        final boolean isVirtualBox = t.getDisplayName().contains("VirtualBox")
                                || t.getDisplayName().contains("Host-only");
                        final boolean hasIpv4 = t.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
                        return !isloopback & supportMulticast & !isVirtualBox & hasIpv4;
                    }catch (Exception e){
                        log.error(e);
                    }
                    return false;
                }
        ).findFirst().orElse(null);
        return networkInterface;
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());
        socket.listen(1234, "0.0.0.0", asyncResult -> {
            if (asyncResult.succeeded()) {
                socket.handler(packet -> {
                    Buffer buffer = packet.data();
                    log.info(new String(buffer.getBytes()));
                });
                try {
                    socket.listenMulticastGroup("230.0.0.1", mainInterface().getName(), null, asyncRes2 -> {
                        log.info("listen success: {}", asyncRes2.succeeded());
                    });
                }catch (Exception e){
                    log.error(e);
                }
            } else {
                System.out.println("Listen failed" + asyncResult.cause());
            }
        });
    }
}
