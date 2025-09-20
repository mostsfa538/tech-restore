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
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

@Component
public class RateLimitingFilter implements Filter {
    
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    
    private final Map<String, AtomicInteger> loginFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> loginFailureBlockTime = new ConcurrentHashMap<>();
    
    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final long TIME_WINDOW_MILLIS = 60000;
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final long LOGIN_BLOCK_DURATION_MILLIS = 600000;
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
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
        String requestURI = httpRequest.getRequestURI();
        
        if (LOGIN_ENDPOINT.equals(requestURI) && isLoginBlocked(clientId, currentTime)) {
            httpResponse.setStatus(TOO_MANY_REQUESTS);
            httpResponse.setContentType("application/json");
            long remainingTime = getRemainingBlockTime(clientId, currentTime);
            httpResponse.getWriter().write(
                String.format("{\"error\":\"Too many failed login attempts\",\"message\":\"Account temporarily locked. Try again in %d minutes.\",\"remainingTime\":%d}", 
                    remainingTime / 60000, remainingTime)
            );
            return;
        }
        
        if (isRateLimited(clientId, currentTime)) {
            httpResponse.setStatus(TOO_MANY_REQUESTS);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}"
            );
            return;
        }
        
        updateRequestCount(clientId, currentTime);
        
        if (LOGIN_ENDPOINT.equals(requestURI)) {
            ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);
            chain.doFilter(request, responseWrapper);
            
            String responseBody = responseWrapper.getResponseBody();
            if (responseWrapper.getStatus() == HttpServletResponse.SC_UNAUTHORIZED || 
                (responseBody != null && responseBody.toLowerCase().contains("invalid_credentials"))) {
                handleLoginFailure(clientId, currentTime);
            } else if (responseWrapper.getStatus() == HttpServletResponse.SC_OK) {
                resetLoginFailures(clientId);
            }
            
            httpResponse.getOutputStream().write(responseWrapper.getResponseBody().getBytes());
        } else {
            chain.doFilter(request, response);
        }
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
    
    private boolean isLoginBlocked(String clientId, long currentTime) {
        Long blockTime = loginFailureBlockTime.get(clientId);
        if (blockTime == null) {
            return false;
        }
        
        return currentTime < blockTime;
    }
    
    private long getRemainingBlockTime(String clientId, long currentTime) {
        Long blockTime = loginFailureBlockTime.get(clientId);
        if (blockTime == null) {
            return 0;
        }
        
        return Math.max(0, blockTime - currentTime);
    }
    
    private void handleLoginFailure(String clientId, long currentTime) {
        AtomicInteger failureCount = loginFailureCounts.computeIfAbsent(clientId, k -> new AtomicInteger(0));
        int newCount = failureCount.incrementAndGet();
        
        if (newCount >= MAX_LOGIN_FAILURES) {
            loginFailureBlockTime.put(clientId, currentTime + LOGIN_BLOCK_DURATION_MILLIS);
        }
    }
    
    private void resetLoginFailures(String clientId) {
        loginFailureCounts.remove(clientId);
        loginFailureBlockTime.remove(clientId);
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
        
        loginFailureBlockTime.entrySet().removeIf(entry -> 
            currentTime > entry.getValue()
        );
        
        loginFailureCounts.entrySet().removeIf(entry -> 
            !loginFailureBlockTime.containsKey(entry.getKey())
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
    
    private static class ResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final PrintWriter writer = new PrintWriter(outputStream);
        private int status = HttpServletResponse.SC_OK;
        
        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        
        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }
        
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                }
                
                @Override
                public boolean isReady() {
                    return true;
                }
                
                @Override
                public void setWriteListener(WriteListener writeListener) {
                    
                }
            };
        }
        
        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }
        
        public int getStatus() {
            return status;
        }
        
        public String getResponseBody() {
            writer.flush();
            return outputStream.toString();
        }
    }
}