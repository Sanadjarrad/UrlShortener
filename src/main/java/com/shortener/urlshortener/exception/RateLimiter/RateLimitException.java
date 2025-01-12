package com.shortener.urlshortener.exception.RateLimiter;


public class RateLimitException extends RuntimeException{
    public RateLimitException(String message) {
        super(message);
    }
}
