package io.todos.loggregator;

import org.cloudfoundry.doppler.Error;

class ErrorMessage {
    private String applicationId;
    private Integer code;
    private String message;
    private String source;

    ErrorMessage(String applicationId, Error error) {
        this.applicationId = applicationId;
        this.code = error.getCode();
        this.message = error.getMessage();
        this.source = error.getSource();
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

}
