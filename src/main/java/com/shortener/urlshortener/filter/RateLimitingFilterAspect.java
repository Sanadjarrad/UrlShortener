package com.shortener.urlshortener.filter;


import com.shortener.urlshortener.exception.RateLimiter.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Aspect
public class RateLimitingFilterAspect {

    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> requestTimestamps = new ConcurrentHashMap<>();
    final static int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    @Value("${rate.limit.max.requests}")
    private int maxRequests;

    @Value("${rate.limit.time.window.ms}")
    private long timeWindowMs;

    @Before("@annotation(com.shortener.urlshortener.filter.RateLimit)")
    public void rateLimit() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) throw new IllegalStateException("Cannot get request attributes");

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        if (response == null) throw new IllegalStateException("Cannot get response attributes");

        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        requestCounts.putIfAbsent(clientIp, new AtomicInteger(0));
        requestTimestamps.putIfAbsent(clientIp, currentTime);

        synchronized (requestCounts.get(clientIp)) {
            if ((currentTime - requestTimestamps.get(clientIp)) > timeWindowMs) {
                requestCounts.get(clientIp).set(0);
                requestTimestamps.put(clientIp, currentTime);
            }

            if (requestCounts.get(clientIp).incrementAndGet() > maxRequests) {
                handleRateLimitExceeded(response, clientIp);
                throw new RateLimitException("Too many requests from IP: " + clientIp);
            }
        }
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String clientIp)  {
        try {
            response.setStatus(HTTP_STATUS_TOO_MANY_REQUESTS);
            response.getWriter().write("Too many requests from IP: " + clientIp + ". Please try again later.");
            response.getWriter().flush();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write rate limit response", e);
        }
    }

    private void cleanUpRequestCounts(final long currentTime) {
        requestCounts.forEach((ip, count) -> {
            if (timeIsTooOld(currentTime, requestTimestamps.getOrDefault(ip, 0L))) {
                count.set(0);
            }
        });
    }

    private boolean timeIsTooOld(final long currentTime, final long timeToCheck) {
        return currentTime - timeToCheck > timeWindowMs;
    }
}

