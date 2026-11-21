package com.myspace.razorpayclone.common.ratelimit;

public interface RateLimiter {
    RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds);
}
