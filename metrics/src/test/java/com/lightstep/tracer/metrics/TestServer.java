package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;
import com.lightstep.tracer.grpc.MetricsServiceGrpc.MetricsServiceImplBase;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

public class TestServer implements AutoCloseable {
  private static Server server;
  private boolean started;

  public TestServer(final int port, final Consumer<IngestRequest> onRequest, final BiConsumer<IngestRequest,IngestResponse> onResponse) throws IOException {
    server = NettyServerBuilder.forPort(port).addService(new MetricsServiceImplBase() {
      @Override
      public void report(final IngestRequest request, final StreamObserver<IngestResponse> responseObserver) {
        onRequest.accept(request);
        if (!started)
          throw new RuntimeException("Server is offline");

        final IngestResponse response = IngestResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        onResponse.accept(request, response);
      }
    }).build().start();
  }

  public void start() {
    started = true;
  }

  @Override
  public void close() throws InterruptedException {
    if (server != null)
      server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }
}