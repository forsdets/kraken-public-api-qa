package com.kraken.publicapi.client.websocketbeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventChannelSubscriptionBeans {

    private int channelID;
    private String channelName;
    private String event;
    private String pair;
    private String status;
    private SubscriptionBeans subscriptionBeans;

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SubscriptionBeans getSubscription() {
        return subscriptionBeans;
    }

    public void setSubscription(SubscriptionBeans subscriptionBeans) {
        this.subscriptionBeans = subscriptionBeans;
    }
}
