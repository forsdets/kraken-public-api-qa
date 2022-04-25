package com.kraken.publicapi.client.websocketbeans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestBeans {

    private String event;
    private List<String> pair;
    @JsonIgnore
    private int requestId;
    private SubscriptionBeans subscriptionBeans;

    public SubscriptionBeans getSubscription() {
        return subscriptionBeans;
    }

    public void setSubscription(SubscriptionBeans subscriptionBeans) {
        this.subscriptionBeans = subscriptionBeans;
    }

    public List<String> getPair() {
        return pair;
    }

    public void setPair(List<String> pair) {
        this.pair = pair;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }


}