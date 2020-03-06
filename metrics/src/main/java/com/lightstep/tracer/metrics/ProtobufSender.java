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
  private final ManagedChannel channel = ManagedChannelBuilder.forAddress("ingest.lightstep.com", 443).usePlaintext().build();
  private final MetricsServiceBlockingStub stub = MetricsServiceGrpc.newBlockingStub(channel);

  ProtobufSender(final String componentName) {
    super(componentName);
  }

  @Override
  final <V extends Number>void createMessage(final IngestRequest.Builder request, final long timestampSeconds, final long durationSeconds, final Metric<?,V> metric, final long current, final long previous) throws IOException {
    final MetricPoint.Builder builder = MetricPoint.newBuilder();
    builder.setKind(MetricKind.DELTA);
    builder.setMetricName(metric.name);

    final Timestamp.Builder timestamp = Timestamp.newBuilder();
    timestamp.setSeconds(timestampSeconds);
//    timestamp.setNanos(timestampNanos);
    builder.setStart(timestamp);

    final Duration.Builder duration = Duration.newBuilder();
    duration.setSeconds(durationSeconds);
//    duration.setNanos(durationNanos);
    builder.setDuration(duration);

    final KeyValue.Builder componentNameLabel = KeyValue.newBuilder();
    componentNameLabel.setKey("lightstep.componentName");
    componentNameLabel.setStringValue(componentName);
    builder.addLabels(componentNameLabel);

    final String hostname = InetAddress.getLocalHost().getHostName();
    final KeyValue.Builder hostnameLabel = KeyValue.newBuilder();
    hostnameLabel.setKey("lightstep.hostname");
    hostnameLabel.setStringValue(hostname);
    builder.addLabels(hostnameLabel);

    final Package mainPackage = ProtobufSender.class.getPackage();
    final String version = mainPackage.getImplementationVersion();
    final String groupId = mainPackage.getName();
    final String artifactId = mainPackage.getImplementationTitle();

    final KeyValue.Builder reporterPlatformLabel = KeyValue.newBuilder();
    reporterPlatformLabel.setKey("lightstep.reporterPlatform");
    reporterPlatformLabel.setStringValue(groupId + ":" + artifactId);
    builder.addLabels(reporterPlatformLabel);

    final KeyValue.Builder reporterPlatformVersionLabel = KeyValue.newBuilder();
    reporterPlatformVersionLabel.setKey("lightstep.reporterPlatformVersion");
    reporterPlatformVersionLabel.setStringValue(version);
    builder.addLabels(reporterPlatformVersionLabel);

    metric.adapter.setValue(builder, metric.compute(current, previous));

    request.addPoints(builder.build());
  }

  @Override
  IngestRequest.Builder newRequest() {
    return IngestRequest.newBuilder();
  }

  @Override
<<<<<<< HEAD
  IngestResponse run(final MetricGroup[] metricGroups, final long timeout) throws Exception {
=======
  public IngestResponse run(final MetricGroup[] metricGroups, final long timeout) throws Exception {
>>>>>>> Extract HardwareAbstractionLayer
    final IngestRequest.Builder request = getRequest();
    request.setIdempotencyKey(UUID.randomUUID().toString());
    return stub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).report(newSampleRequest(metricGroups, request).build());
  }
}