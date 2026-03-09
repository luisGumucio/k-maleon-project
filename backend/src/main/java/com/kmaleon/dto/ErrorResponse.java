package com.kmaleon.dto;

import java.time.OffsetDateTime;

public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private OffsetDateTime timestamp;

    private ErrorResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ErrorResponse response = new ErrorResponse();

        public Builder status(int status) { response.status = status; return this; }
        public Builder error(String error) { response.error = error; return this; }
        public Builder message(String message) { response.message = message; return this; }
        public Builder timestamp(OffsetDateTime timestamp) { response.timestamp = timestamp; return this; }

        public ErrorResponse build() { return response; }
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public OffsetDateTime getTimestamp() { return timestamp; }
}
