package com.lightstep.tracer.metrics;

import static com.lightstep.tracer.shared.LightStepConstants.Tags.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;
import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.MetricKind;
import com.lightstep.tracer.grpc.MetricPoint;
import com.lightstep.tracer.grpc.Reporter;

abstract class ProtobufSender extends Sender<IngestRequest.Builder,IngestResponse> {
  private final Reporter.Builder reporter;
  private final KeyValue.Builder[] labels;

  // TODO: Unify the constants.
  ProtobufSender(final String componentName, final String accessToken, final String serviceUrl, final boolean sendFirstReport) {
    super(componentName, accessToken, serviceUrl, sendFirstReport);

    final String hostname = getHostname();

    // TODO: Where to get the service version from?
    reporter = Reporter.newBuilder();
    //reporter.addTags(KeyValue.newBuilder().setKey("service.version").setStringValue("vTest"));
    reporter.addTags(KeyValue.newBuilder().setKey(COMPONENT_NAME_KEY).setStringValue(componentName));
    reporter.addTags(KeyValue.newBuilder().setKey("lightstep.hostname").setStringValue(hostname));
    reporter.addTags(KeyValue.newBuilder().setKey("lightstep.reporter_platform").setStringValue("java"));
    reporter.addTags(KeyValue.newBuilder().setKey("lightstep.reporter_platform_version").setStringValue(getJavaVersion()));

    labels = new KeyValue.Builder[] {
      //KeyValue.newBuilder().setKey("service.version").setStringValue("vTest"),
      KeyValue.newBuilder().setKey(COMPONENT_NAME_KEY).setStringValue(componentName),
      KeyValue.newBuilder().setKey("lightstep.hostname").setStringValue(hostname)
    };
  }

  @Override
  final <V extends Number>void createMessage(final IngestRequest.Builder request, final long timestampSeconds, final long durationSeconds, final Metric<?,V> metric, final long current, final long previous) throws IOException {
    final MetricPoint.Builder builder = MetricPoint.newBuilder();
    builder.setMetricName(metric.getName());

    final Timestamp.Builder timestamp = Timestamp.newBuilder();
    timestamp.setSeconds(timestampSeconds);
    builder.setStart(timestamp);

    final Duration.Builder duration = Duration.newBuilder();
    duration.setSeconds(durationSeconds);
    builder.setDuration(duration);

    builder.setDoubleValue(metric.getValue(current, previous));
//    metric.getAdapter().setValue(builder, metric.compute(current, previous));

    if (metric instanceof CounterMetric)
      builder.setKind(MetricKind.COUNTER);
    else
      builder.setKind(MetricKind.GAUGE);

    // Add the predefined labels.
    for (int i = 0; i < labels.length; ++i) {
      builder.addLabels(labels[i]);
    }

    request.addPoints(builder.build());
  }

  private static String getHostname() {
    // FIXME: Technically, the following line is the proper "java way" to get
    // the hostname. However, this most always returns an internal IP address,
    // which may be incorrect for our needs?!
    try {
      return InetAddress.getLocalHost().getHostName();
    }
    catch (final IOException e) {
      return "";
    }
  }

  private static String getJavaVersion() {
    return System.getProperty("java.version");
  }

  @Override
  IngestRequest.Builder newRequest() {
    return IngestRequest.newBuilder();
  }

  @Override
  IngestRequest.Builder setIdempotency(final IngestRequest.Builder request) {
    return request.setIdempotencyKey(UUID.randomUUID().toString());
  }

  @Override
  IngestRequest.Builder setReporter(final IngestRequest.Builder request) {
    return request.setReporter(reporter);
  }
}