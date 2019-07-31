package io.todos.loggregator;

import org.cloudfoundry.doppler.Envelope;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

class SourceContext {
    private String appId;
    private Flux<Envelope> sourceStream;
    private Disposable disposable;
    SourceContext(String appId, Flux<Envelope> sourceStream, Disposable disposable) {
        this.appId = appId;
        this.sourceStream = sourceStream;
        this.disposable = disposable;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Flux<Envelope> getSourceStream() {
        return sourceStream;
    }

    public void setSourceStream(Flux<Envelope> sourceStream) {
        this.sourceStream = sourceStream;
    }

    public Disposable getDisposable() {
        return disposable;
    }

    public void setDisposable(Disposable dispose) {
        this.disposable = dispose;
    }
}
