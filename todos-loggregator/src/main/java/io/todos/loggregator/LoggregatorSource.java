package io.todos.loggregator;

import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.cloudfoundry.doppler.StreamRequest;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@RestController
@EnableBinding(LoggregatorSource.SourceChannels.class)
public class LoggregatorSource {

    private static final Logger LOG = LoggerFactory.getLogger(LoggregatorSource.class);

    interface SourceChannels {
        @Output
        MessageChannel logEvents();
        @Output
        MessageChannel errorEvents();
        @Output
        MessageChannel containerMetricEvents();
    }
    private SourceChannels channels;

    private ReactorDopplerClient dopplerClient;

    private Map<String, SourceContext> streamIndex = new HashMap<>();

    @Autowired
    public LoggregatorSource(SourceChannels channels, ReactorDopplerClient dopplerClient) {
        this.channels = channels;
        this.dopplerClient = dopplerClient;
    }

    @PutMapping("/start/{appId}")
    void start(@PathVariable String appId) {
        LOG.info(appId + ": Starting logStreams for app " + appId);
        Flux<Envelope> dopplerStream = dopplerClient.stream(StreamRequest.builder().applicationId(appId).build());
        Disposable disposable = dopplerStream.subscribeOn(Schedulers.elastic())
            .subscribe(it -> {
                LOG.info(appId + ":" + it.toString());
                if(it.getEventType() == EventType.CONTAINER_METRIC) {
                    channels.containerMetricEvents().send(
                        MessageFactory.createContainerMetricMessage(it.getContainerMetric()));
                } else if(it.getEventType() == EventType.LOG_MESSAGE) {
                    channels.logEvents().send(
                        MessageFactory.createLogEventMessage(it.getLogMessage()));
                } else if(it.getEventType() == EventType.ERROR) {
                    channels.errorEvents().send(
                        MessageFactory.createErrorMessage(appId, it.getError()));
                }
            });
        LOG.info(appId + ": Storing SourceContext for app " + appId);
        streamIndex.put(appId, new SourceContext(appId, dopplerStream, disposable));
    }

    @PutMapping("/stop/{appId}")
    void stop(@PathVariable String appId) {
        LOG.info(appId + ": Stopping logStreams for app " + appId);
        streamIndex.get(appId).getDisposable().dispose();
    }
}
