package io.todos.loggregator;

import org.cloudfoundry.doppler.LogMessage;

public class LogEventMessage {

    private String applicationId;
    private String message;
    private Long timestamp;
    private String sourceInstance;

    LogEventMessage(LogMessage logMessage) {
        this.applicationId = logMessage.getApplicationId();
        this.message = logMessage.getMessage();
        this.timestamp = logMessage.getTimestamp();
        this.sourceInstance = logMessage.getSourceInstance();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceInstance() {
        return sourceInstance;
    }

    public void setSourceInstance(String sourceInstance) {
        this.sourceInstance = sourceInstance;
    }

}
