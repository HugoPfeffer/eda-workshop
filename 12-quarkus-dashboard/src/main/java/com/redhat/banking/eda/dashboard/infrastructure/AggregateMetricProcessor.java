package com.redhat.banking.eda.dashboard.infrastructure;

import io.quarkus.infinispan.client.Remote;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.banking.eda.dashboard.valueobjects.AggregateMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bean consuming data from the "prices" Kafka topic and applying some conversion.
 * The result is pushed to the "my-data-stream" stream which is an in-memory stream.
 */
@ApplicationScoped
public class AggregateMetricProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateMetricProcessor.class);

    @Inject 
    @Remote("aggregate-metrics")
    RemoteCache<String, AggregateMetric> cache;

    RemoteCacheManager remoteCacheManager;
    
    @Inject 
    AggregateMetricProcessor(RemoteCacheManager remoteCacheManager) {
        this.remoteCacheManager = remoteCacheManager;
    }
    
    @Incoming("aggregate-metrics")                                     
    @Outgoing("aggregate-metrics-stream")                             
    @Broadcast                                              
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING) 
    public AggregateMetric process(AggregateMetric metric) {
        LOG.info("Processing Aggregated Metric {}", metric);
        cache.put(metric.getTimestamp().toString(), metric);
        LOG.info("size = " + cache.size());
        return metric;
    }

}
