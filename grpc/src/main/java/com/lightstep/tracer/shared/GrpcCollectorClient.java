package com.lightstep.tracer.shared;

import com.google.protobuf.ByteString;
import com.lightstep.tracer.grpc.CollectorServiceGrpc;
import com.lightstep.tracer.grpc.CollectorServiceGrpc.CollectorServiceBlockingStub;
import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;
import com.lightstep.tracer.grpc.Span;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.concurrent.TimeUnit;

class GrpcCollectorClient extends CollectorClient {
  private final ManagedChannelBuilder<?> channelBuilder;
  private ManagedChannel channel;
  private CollectorServiceBlockingStub blockingStub;
  private final ClientMetrics clientMetrics;
  private final AbstractTracer tracer;
  private final long deadlineMillis;

  /**
   * Constructor client for accessing CollectorService using the existing channel
   */
  GrpcCollectorClient(
          AbstractTracer tracer,
          ManagedChannelBuilder channelBuilder,
          long deadlineMillis,
          ClientMetrics clientMetrics
  ) {
    this.tracer = tracer;
    this.channelBuilder = channelBuilder;
    this.deadlineMillis = deadlineMillis;
    this.clientMetrics = clientMetrics;

    connect();
  }

  synchronized void shutdown() {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      tracer.warn("Interrupted while shutting down client, force shutdown now!");
      channel.shutdownNow();
    }
  }

  /**
   * Blocking call to report
   */
  synchronized ReportResponse report(ReportRequest request) {
    // send report to collector
    boolean success = false;
    try {
      ReportResponse response = blockingStub.
              withDeadlineAfter(deadlineMillis, TimeUnit.MILLISECONDS).
              report(request);

      // check response for errors
      if (response.getErrorsCount() != 0) {
        List<ByteString> errs = response.getErrorsList().asByteStringList();
        for (ByteString err : errs) {
          tracer.error("Collector response contained error: ", err.toString());
        }
      } else {
        success = true;
      }

      return response;
    } catch (StatusRuntimeException e) {
      tracer.error("Status runtime exception (likely malformed spans): ", e);
    } catch (RuntimeException e) {
      tracer.error("Runtime exception: ", e);
    } catch (Exception e) {
      tracer.error("Exception sending report to collector: ", e);
    } finally {
      if (!success) {
        recordDroppedSpans(request.getSpansList());
      }
    }
    return null;
  }

  private synchronized void connect() {
    channel = channelBuilder.build();
    blockingStub = CollectorServiceGrpc.newBlockingStub(channel);
  }

  synchronized void reconnect() {
    this.shutdown();
    this.connect();
  }

  private void recordDroppedSpans(List<Span> spans) {
    clientMetrics.addSpansDropped(spans.size());
  }
}
