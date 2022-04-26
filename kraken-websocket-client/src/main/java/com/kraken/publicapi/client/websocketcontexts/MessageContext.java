package com.kraken.publicapi.client.websocketcontexts;

import java.time.LocalDateTime;

public class MessageContext {
	
	private String receivedMessage;
	private LocalDateTime receivedDateTime;
	
	public MessageContext(String receivedMessage, LocalDateTime receivedDateTime)
	{
		this.receivedMessage = receivedMessage;
		this.receivedDateTime = receivedDateTime;
	}
	
	public String getReceivedMessage() {
		return receivedMessage;
	}
	public void setReceivedMessage(String receivedMessage) {
		this.receivedMessage = receivedMessage;
	}
	public LocalDateTime getReceivedDateTime() {
		return receivedDateTime;
	}
	public void setReceivedDateTime(LocalDateTime receivedDateTime) {
		this.receivedDateTime = receivedDateTime;
	}
}
