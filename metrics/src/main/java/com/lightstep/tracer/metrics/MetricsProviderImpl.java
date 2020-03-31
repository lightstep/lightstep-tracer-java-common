package com.lightstep.tracer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstep.tracer.shared.MetricsProvider;
import com.lightstep.tracer.shared.SafeMetrics;
import com.lightstep.tracer.metrics.OkHttpSender;
import com.lightstep.tracer.metrics.Sender;

// TODO: Tests for this class!
// TODO: And tests for the AbstractTracer working just fine!
public class MetricsProviderImpl extends MetricsProvider {
    @Override
    public SafeMetrics create() {
        return new SafeMetricsImpl();
    }
}
