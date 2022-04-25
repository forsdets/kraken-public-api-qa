package com.kraken.publicapi.client.websocketbeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusMessageBeans {

    private String errorMessage;
    private String event;
    private String pair;
    private String status;
    private SubscriptionBeans subscriptionBeans;

    public SubscriptionBeans getSubscription() {
        return subscriptionBeans;
    }

    public void setSubscription(SubscriptionBeans subscriptionBeans) {
        this.subscriptionBeans = subscriptionBeans;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

}
