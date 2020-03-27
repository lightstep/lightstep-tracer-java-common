package com.lightstep.tracer.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.23.0)",
    comments = "Source: metrics.proto")
public final class MetricsServiceGrpc {

  private MetricsServiceGrpc() {}

  public static final String SERVICE_NAME = "lightstep.metrics.MetricsService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.lightstep.tracer.grpc.IngestRequest,
      com.lightstep.tracer.grpc.IngestResponse> getReportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Report",
      requestType = com.lightstep.tracer.grpc.IngestRequest.class,
      responseType = com.lightstep.tracer.grpc.IngestResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.lightstep.tracer.grpc.IngestRequest,
      com.lightstep.tracer.grpc.IngestResponse> getReportMethod() {
    io.grpc.MethodDescriptor<com.lightstep.tracer.grpc.IngestRequest, com.lightstep.tracer.grpc.IngestResponse> getReportMethod;
    if ((getReportMethod = MetricsServiceGrpc.getReportMethod) == null) {
      synchronized (MetricsServiceGrpc.class) {
        if ((getReportMethod = MetricsServiceGrpc.getReportMethod) == null) {
          MetricsServiceGrpc.getReportMethod = getReportMethod =
              io.grpc.MethodDescriptor.<com.lightstep.tracer.grpc.IngestRequest, com.lightstep.tracer.grpc.IngestResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Report"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lightstep.tracer.grpc.IngestRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lightstep.tracer.grpc.IngestResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MetricsServiceMethodDescriptorSupplier("Report"))
              .build();
        }
      }
    }
    return getReportMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MetricsServiceStub newStub(io.grpc.Channel channel) {
    return new MetricsServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MetricsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new MetricsServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MetricsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new MetricsServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class MetricsServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void report(com.lightstep.tracer.grpc.IngestRequest request,
        io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.IngestResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getReportMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getReportMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.lightstep.tracer.grpc.IngestRequest,
                com.lightstep.tracer.grpc.IngestResponse>(
                  this, METHODID_REPORT)))
          .build();
    }
  }

  /**
   */
  public static final class MetricsServiceStub extends io.grpc.stub.AbstractStub<MetricsServiceStub> {
    private MetricsServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MetricsServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MetricsServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MetricsServiceStub(channel, callOptions);
    }

    /**
     */
    public void report(com.lightstep.tracer.grpc.IngestRequest request,
        io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.IngestResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getReportMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class MetricsServiceBlockingStub extends io.grpc.stub.AbstractStub<MetricsServiceBlockingStub> {
    private MetricsServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MetricsServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MetricsServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MetricsServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.lightstep.tracer.grpc.IngestResponse report(com.lightstep.tracer.grpc.IngestRequest request) {
      return blockingUnaryCall(
          getChannel(), getReportMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class MetricsServiceFutureStub extends io.grpc.stub.AbstractStub<MetricsServiceFutureStub> {
    private MetricsServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MetricsServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MetricsServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MetricsServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.lightstep.tracer.grpc.IngestResponse> report(
        com.lightstep.tracer.grpc.IngestRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getReportMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REPORT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final MetricsServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(MetricsServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REPORT:
          serviceImpl.report((com.lightstep.tracer.grpc.IngestRequest) request,
              (io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.IngestResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class MetricsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MetricsServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.lightstep.tracer.grpc.Metrics.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MetricsService");
    }
  }

  private static final class MetricsServiceFileDescriptorSupplier
      extends MetricsServiceBaseDescriptorSupplier {
    MetricsServiceFileDescriptorSupplier() {}
  }

  private static final class MetricsServiceMethodDescriptorSupplier
      extends MetricsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    MetricsServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MetricsServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MetricsServiceFileDescriptorSupplier())
              .addMethod(getReportMethod())
              .build();
        }
      }
    }
    return result;
  }
}
