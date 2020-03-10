package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;
import com.lightstep.tracer.grpc.MetricsServiceGrpc.MetricsServiceImplBase;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

public class TestServer implements AutoCloseable {
  private static Server server;
  private boolean offline = true;

  public TestServer(final int port, final BiConsumer<IngestRequest,IngestResponse> onResponse) throws IOException {
    server = NettyServerBuilder.forPort(port).addService(new MetricsServiceImplBase() {
      @Override
      public void report(final IngestRequest request, final StreamObserver<IngestResponse> responseObserver) {
        if (offline)
          throw new RuntimeException("Server is offline");

        final IngestResponse response = IngestResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        onResponse.accept(request, response);
      }
    }).build().start();
  }

  public void start() {
    offline = false;
  }

  @Override
  public void close() throws InterruptedException {
    if (server != null)
      server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }
}