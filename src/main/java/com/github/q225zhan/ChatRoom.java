package com.github.q225zhan;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ChatRoom
 *
 * @version 1.0 - 2017-01-28
 */
public class ChatRoom {

    private static final int MAX_ONLINE = 3;

    public static void main(String[] args) throws InterruptedException {
        final AtomicInteger onlineCounter = new AtomicInteger(0);

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);

        final SocketIOServer server = new SocketIOServer(config);

        server.addConnectListener(client -> {
            if (onlineCounter.get() >= MAX_ONLINE) {
                ChatObject maxWarning = new ChatObject();
                maxWarning.setUserName("Admin");
                maxWarning.setMessage("chatroom is full.");
                client.sendEvent("chatevent", maxWarning);
                client.disconnect();
            }
            onlineCounter.incrementAndGet();
        });

        server.addDisconnectListener(client -> {
            onlineCounter.decrementAndGet();
        });

        server.addEventListener("chatevent", ChatObject.class, (client, data, ackRequest) -> {
            // broadcast messages to all clients
            server.getBroadcastOperations().sendEvent("chatevent", data);
        });

        server.start();

        // add Shutdown hook
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}

