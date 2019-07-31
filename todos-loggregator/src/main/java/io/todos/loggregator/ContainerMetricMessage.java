package io.todos.loggregator;

import org.cloudfoundry.doppler.ContainerMetric;

class ContainerMetricMessage {

    private String applicationId;
    private Double cpuPercentage;
    private Long diskBytes;
    private Integer instanceIndex;
    private Long memoryBytes;
    private Long memoryBytesQuota;

    ContainerMetricMessage(ContainerMetric containerMetric) {
        this.applicationId = containerMetric.getApplicationId();
        this.cpuPercentage = containerMetric.getCpuPercentage();
        this.diskBytes = containerMetric.getDiskBytes();
        this.instanceIndex = containerMetric.getInstanceIndex();
        this.memoryBytes = containerMetric.getMemoryBytes();
        this.memoryBytesQuota = containerMetric.getMemoryBytesQuota();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public Double getCpuPercentage() {
        return cpuPercentage;
    }

    public Long getDiskBytes() {
        return diskBytes;
    }

    public Integer getInstanceIndex() {
        return instanceIndex;
    }

    public Long getMemoryBytes() {
        return memoryBytes;
    }

    public Long getMemoryBytesQuota() {
        return memoryBytesQuota;
    }
}
