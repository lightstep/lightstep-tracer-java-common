package com.lightstep.tracer.metrics;

import java.util.concurrent.TimeUnit;

import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;
import com.lightstep.tracer.grpc.MetricsServiceGrpc;
import com.lightstep.tracer.grpc.MetricsServiceGrpc.MetricsServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

// Sender used for testing purposes.
// Consider removing it once we have an OKHttp server with
// its own tests.
public class GrpcSender extends ProtobufSender {
  private final ManagedChannel channel;
  private final MetricsServiceBlockingStub stub;
  private final String servicePath;
  private final int servicePort;

  public GrpcSender(final String componentName, final String accessToken, final String serviceUrl, final boolean sendFirstReport) {
      this(componentName, accessToken, "", serviceUrl, sendFirstReport);
  }

  public GrpcSender(final String componentName, final String accessToken, final String serviceVersion, final String serviceUrl, final boolean sendFirstReport) {
    super(componentName, accessToken, serviceVersion, serviceUrl, sendFirstReport);

    final int colon = serviceUrl.indexOf(":");
    servicePath = serviceUrl.substring(0, colon);
    servicePort = Integer.valueOf(serviceUrl.substring(colon + 1));
    channel = ManagedChannelBuilder.forAddress(servicePath, servicePort).usePlaintext().build();
    stub = MetricsServiceGrpc.newBlockingStub(channel);
  }

  @Override
  IngestResponse invoke(final IngestRequest.Builder request, final long timeout) throws Exception {
    return stub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).report(request.build());
  }

  @Override
  public void close() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }
}
