package com.kraken.publicapi.client.websocketbeans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionBeans {

    private String name;
    @JsonIgnore
    private int depth;
    @JsonIgnore
    private int interval;
    @JsonIgnore
    private Boolean rateCounter;
    @JsonIgnore
    private Boolean snapshot;
    @JsonIgnore
    private String token;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Boolean snapshot) {
        this.snapshot = snapshot;
    }

    public Boolean getRateCounter() {
        return rateCounter;
    }

    public void setRateCounter(Boolean rateCounter) {
        this.rateCounter = rateCounter;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
