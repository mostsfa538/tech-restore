package com.techRestore.tech.restore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
    String error,
    String message,
    @JsonProperty("error_code") String errorCode
) {}
