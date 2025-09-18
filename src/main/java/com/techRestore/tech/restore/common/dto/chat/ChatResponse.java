package com.techRestore.tech.restore.common.dto.chat;

import lombok.Data;

@Data
public class ChatResponse {
    private boolean success;
    private String message;
    private Object data;
    
    public static ChatResponse success(String message, Object data) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    public static ChatResponse error(String message) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}