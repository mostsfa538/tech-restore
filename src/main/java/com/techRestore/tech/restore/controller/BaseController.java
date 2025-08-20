package com.techRestore.tech.restore.controller;

import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    
    /**
     * Standard success response for GET operations
     */
    protected <T> ResponseEntity<T> successResponse(T body) {
        return ResponseEntity.ok(body);
    }
    
    /**
     * Standard success response for POST operations
     */
    protected <T> ResponseEntity<T> createdResponse(T body) {
        return ResponseEntity.status(201).body(body);
    }
    
    /**
     * Standard success response for PUT/PATCH operations
     */
    protected <T> ResponseEntity<T> updatedResponse() {
        return ResponseEntity.status(200).build();
    }
    
    protected <T> ResponseEntity<T> updatedResponse(T body) {
        return ResponseEntity.status(200).body(body);
    }

    /**
     * Standard success response for DELETE operations
     */
    protected ResponseEntity<Void> deletedResponse() {
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Standard success response with custom message
     */
    protected ResponseEntity<String> successMessageResponse(String message) {
        return ResponseEntity.ok().body(message);
    }
}