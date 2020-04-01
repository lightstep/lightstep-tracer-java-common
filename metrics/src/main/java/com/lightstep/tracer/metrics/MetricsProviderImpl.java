package com.lightstep.tracer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstep.tracer.shared.MetricsProvider;
import com.lightstep.tracer.shared.SafeMetrics;
import com.lightstep.tracer.metrics.OkHttpSender;
import com.lightstep.tracer.metrics.Sender;

public class MetricsProviderImpl extends MetricsProvider {
    @Override
    public SafeMetrics create() {
        return new SafeMetricsImpl();
    }
}
