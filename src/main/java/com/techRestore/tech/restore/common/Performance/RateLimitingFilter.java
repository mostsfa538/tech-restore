package com.techRestore.tech.restore.common.Performance;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter implements Filter {
    
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    
    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final long TIME_WINDOW_MILLIS = 60000;
    private static final int TOO_MANY_REQUESTS = 429;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public RateLimitingFilter() {
        scheduler.scheduleAtFixedRate(this::cleanupOldEntries, 1, 1, TimeUnit.MINUTES);
    }
    

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = getClientIdentifier(httpRequest);
        long currentTime = System.currentTimeMillis();
        
        if (isRateLimited(clientId, currentTime)) {
            httpResponse.setStatus(TOO_MANY_REQUESTS);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}"
            );
            return;
        }
        updateRequestCount(clientId, currentTime);
        chain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
    
    private boolean isRateLimited(String clientId, long currentTime) {
        Long lastTime = lastRequestTime.get(clientId);
        AtomicInteger count = requestCounts.get(clientId);
        
        if (lastTime == null || currentTime - lastTime > TIME_WINDOW_MILLIS) {
            return false;
        }
        
        return count != null && count.get() >= MAX_REQUESTS_PER_MINUTE;
    }
    
    private void updateRequestCount(String clientId, long currentTime) {
        Long lastTime = lastRequestTime.get(clientId);
        
        if (lastTime == null || currentTime - lastTime > TIME_WINDOW_MILLIS) {
            requestCounts.put(clientId, new AtomicInteger(1));
            lastRequestTime.put(clientId, currentTime);
        } else {
            requestCounts.computeIfAbsent(clientId, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }
    
    private void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        
        lastRequestTime.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > TIME_WINDOW_MILLIS * 2
        );
        requestCounts.entrySet().removeIf(entry -> 
            !lastRequestTime.containsKey(entry.getKey())
        );
    }
    
    @Override
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}