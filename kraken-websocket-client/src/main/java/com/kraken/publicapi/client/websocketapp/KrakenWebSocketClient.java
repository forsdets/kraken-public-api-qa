package com.kraken.publicapi.client.websocketapp;

import com.kraken.publicapi.client.websocketcontexts.MessageContext;
import com.kraken.publicapi.client.websocketcontexts.SocketDataContext;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;

public class KrakenWebSocketClient extends WebSocketClient {

    Logger logger = LoggerFactory.getLogger(KrakenWebSocketClient.class);

    public SocketDataContext socketDataContext;
    public LocalDateTime openedTime, closedTime;

    public KrakenWebSocketClient(SocketDataContext socketDataContext) throws URISyntaxException {
        super(new URI(socketDataContext.getURI()));
        this.socketDataContext = socketDataContext;
    }

    @Override
    public void onOpen(ServerHandshake serverHandShake) {
        this.openedTime = LocalDateTime.now();
        logger.info(String.format("WebSocket is connected to '%s' at %s", socketDataContext.getURI(), this.openedTime.toString()));
    }

    @Override
    public void onMessage(String message) {

        LocalDateTime dt = LocalDateTime.now();
        logger.info(String.format("Message '%s' received at %s", message, dt));

        MessageContext messageContext = new MessageContext(message, dt);
        socketDataContext.addMessage(messageContext);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.closedTime = LocalDateTime.now();
        logger.info(String.format("Connection Active Time : %d", getSocketConnectionActiveTime()));
        logger.info(String.format("Connection Closed Time : %s", this.closedTime.toString()));
        if (remote) {
            logger.info(String.format("Connection is closed by host due to '%s' at %s", reason, this.closedTime.toString()));
        }
        socketDataContext.setStatusCode(code);
    }

    @Override
    public void onError(Exception exception) {
        logger.info(String.format("Connection issue %s", exception.getMessage()));
    }

    public int getSocketConnectionActiveTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        if (this.isClosed()) {
            dateTime = this.closedTime;
        }
        int timeInSeconds = (int) Duration.between(openedTime, dateTime).getSeconds();
        socketDataContext.setTimeTaken(timeInSeconds);
        return timeInSeconds;
    }
}
