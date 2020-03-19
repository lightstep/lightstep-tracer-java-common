package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;
import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.MetricKind;
import com.lightstep.tracer.grpc.MetricPoint;
import com.lightstep.tracer.grpc.MetricsServiceGrpc;
import com.lightstep.tracer.grpc.MetricsServiceGrpc.MetricsServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

class ProtobufSender extends Sender<IngestRequest.Builder,IngestResponse> {
  private final ManagedChannel channel;
  private final MetricsServiceBlockingStub stub;

  ProtobufSender(final String componentName, final String hostName, final int port) {
    super(componentName, hostName, port);
    channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext().build();
    stub = MetricsServiceGrpc.newBlockingStub(channel);
  }

  @Override
  final <V extends Number>void createMessage(final IngestRequest.Builder request, final long timestampSeconds, final long durationSeconds, final Metric<?,V> metric, final long current, final long previous) throws IOException {
    final MetricPoint.Builder builder = MetricPoint.newBuilder();
    builder.setKind(MetricKind.DELTA);
    builder.setMetricName(metric.getName());

    final Timestamp.Builder timestamp = Timestamp.newBuilder();
    timestamp.setSeconds(timestampSeconds);
//    timestamp.setNanos(timestampNanos);
    builder.setStart(timestamp);

    final Duration.Builder duration = Duration.newBuilder();
    duration.setSeconds(durationSeconds);
//    duration.setNanos(durationNanos);
    builder.setDuration(duration);

    final KeyValue.Builder componentNameLabel = KeyValue.newBuilder();
    componentNameLabel.setKey("lightstep.component_name");
    componentNameLabel.setStringValue(componentName);
    builder.addLabels(componentNameLabel);

    // FIXME: Technically, the following line is the proper "java way" to get the hostname. However, this most always returns an internal IP address, which may be incorrect for our needs?!
    final String hostname = InetAddress.getLocalHost().getHostName();
    final KeyValue.Builder hostnameLabel = KeyValue.newBuilder();
    hostnameLabel.setKey("lightstep.hostname");
    hostnameLabel.setStringValue(hostname);
    builder.addLabels(hostnameLabel);

    final KeyValue.Builder reporterPlatformLabel = KeyValue.newBuilder();
    reporterPlatformLabel.setKey("lightstep.reporter_platform");
    reporterPlatformLabel.setStringValue(getPlatformReporter());
    builder.addLabels(reporterPlatformLabel);

    final KeyValue.Builder reporterPlatformVersionLabel = KeyValue.newBuilder();
    reporterPlatformVersionLabel.setKey("lightstep.reporter_platform_version");
    reporterPlatformVersionLabel.setStringValue(getPlatformVersion());
    builder.addLabels(reporterPlatformVersionLabel);

    metric.getAdapter().setValue(builder, metric.compute(current, previous));

    request.addPoints(builder.build());
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
  IngestResponse invoke(final long timeout) throws Exception {
    final IngestRequest.Builder request = getRequest();
    if (request == null)
      throw new IllegalStateException("Request should not be null");

    return stub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).report(request.build());
  }

  @Override
  public void close() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }
}