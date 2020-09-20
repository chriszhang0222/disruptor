package com.chris.TCP;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TCPServer {
    public static Vertx vertx = Vertx.vertx();
    public static NetSocket socket = null;

    public static void main(String[] args) {
        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(new Handler<NetSocket>(){
            @Override
            public void handle(NetSocket event) {

            }
        });
        netServer.listen(9009, "localhost", res -> {
            if (res.succeeded()) {
                log.info("gateway startup success at port : {}", 9009);
            } else {
                log.error("gateway startup fail");
            }
        });


    }
}
