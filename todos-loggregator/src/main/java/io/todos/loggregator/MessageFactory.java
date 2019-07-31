package io.todos.loggregator;

import org.cloudfoundry.doppler.ContainerMetric;
import org.cloudfoundry.doppler.Error;
import org.cloudfoundry.doppler.LogMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

class MessageFactory {
    static Message<ContainerMetricMessage> createContainerMetricMessage(ContainerMetric containerMetric) {
        return new GenericMessage<>(new ContainerMetricMessage(containerMetric));
    }

    static Message<LogEventMessage> createLogEventMessage(
        LogMessage logMessage) {
        return new GenericMessage<>(new LogEventMessage(logMessage));
    }

    static Message<ErrorMessage> createErrorMessage(
        String appId, Error error) {
        return new GenericMessage<>(new ErrorMessage(appId, error));
    }
}
