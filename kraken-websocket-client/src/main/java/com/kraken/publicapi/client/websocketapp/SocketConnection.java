package com.kraken.publicapi.client.websocketapp;

import com.kraken.publicapi.client.websocketcontexts.SocketDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

public class SocketConnection {

    Logger logger = LoggerFactory.getLogger(SocketConnection.class);
    private KrakenWebSocketClient krakenWebSocketClient;

    public KrakenWebSocketClient getKrakenWebSocketClient() {
        return krakenWebSocketClient;
    }

    public SocketConnection() {
    }

    public SocketConnection connectToHost(SocketDataContext socketContext) {
        try {
            this.krakenWebSocketClient = new KrakenWebSocketClient(socketContext);
            this.krakenWebSocketClient.connectBlocking();
            while (this.krakenWebSocketClient.socketDataContext.getMessageList().size() <= 0) {
                Thread.sleep(100);
            }
        } catch (URISyntaxException | InterruptedException e) {
            logger.info("Connection error - %s", e);
        }
        return this;
    }

    public SocketConnection subscribeMessage(String subscribeRequest) throws InterruptedException {
        if (this.krakenWebSocketClient.isOpen()) {
            int currentMessageSize = this.krakenWebSocketClient.socketDataContext.getMessageList().size();
            this.krakenWebSocketClient.send(subscribeRequest);
            logger.info(String.format("Message - '%s' is subscribed by the host", subscribeRequest));
            while (this.krakenWebSocketClient.socketDataContext.getMessageList().size() <= currentMessageSize) {
                Thread.sleep(100);
            }
        } else {
            logger.info(String.format("The Socket Connection '%s' is already terminated", this.krakenWebSocketClient.socketDataContext.getURI()));
        }

        return this;
    }

    public SocketConnection unsubscribeMessage(String unSubscribeRequest) throws InterruptedException {
        if (this.krakenWebSocketClient.isOpen()) {
            int currentMsqQueueSize = this.krakenWebSocketClient.socketDataContext.getMessageList().size();
            this.krakenWebSocketClient.send(unSubscribeRequest);
            logger.info(String.format("Message - '%s' is unsubscribed by the host", unSubscribeRequest));
            while (this.krakenWebSocketClient.socketDataContext.getMessageList().size() <= currentMsqQueueSize) {
                Thread.sleep(100);
            }
        } else {
            logger.info(String.format("Connection to '%s' is already terminated", this.krakenWebSocketClient.socketDataContext.getURI()));
        }
        return this;
    }

    public void closeConnection() throws InterruptedException {

        if (this.krakenWebSocketClient.isOpen()) {
            this.krakenWebSocketClient.closeBlocking();
        } else {
            logger.info(String.format("The WebSocket connection is already shutdown - '%s'", this.krakenWebSocketClient.socketDataContext.getURI()));
        }
    }
}
