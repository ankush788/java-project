package com.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private long defaultCapacity = 100;
    private long defaultRefillRate = 10;
    private long defaultWindowSeconds = 60;

    public long getDefaultCapacity() {
        return defaultCapacity;
    }

    public void setDefaultCapacity(long defaultCapacity) {
        this.defaultCapacity = defaultCapacity;
    }

    public long getDefaultRefillRate() {
        return defaultRefillRate;
    }

    public void setDefaultRefillRate(long defaultRefillRate) {
        this.defaultRefillRate = defaultRefillRate;
    }

    public long getDefaultWindowSeconds() {
        return defaultWindowSeconds;
    }

    public void setDefaultWindowSeconds(long defaultWindowSeconds) {
        this.defaultWindowSeconds = defaultWindowSeconds;
    }
}
