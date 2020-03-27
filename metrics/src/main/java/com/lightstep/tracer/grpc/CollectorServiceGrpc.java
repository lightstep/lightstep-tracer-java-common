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
    comments = "Source: collector.proto")
public final class CollectorServiceGrpc {

  private CollectorServiceGrpc() {}

  public static final String SERVICE_NAME = "lightstep.collector.CollectorService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.lightstep.tracer.grpc.ReportRequest,
      com.lightstep.tracer.grpc.ReportResponse> getReportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Report",
      requestType = com.lightstep.tracer.grpc.ReportRequest.class,
      responseType = com.lightstep.tracer.grpc.ReportResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.lightstep.tracer.grpc.ReportRequest,
      com.lightstep.tracer.grpc.ReportResponse> getReportMethod() {
    io.grpc.MethodDescriptor<com.lightstep.tracer.grpc.ReportRequest, com.lightstep.tracer.grpc.ReportResponse> getReportMethod;
    if ((getReportMethod = CollectorServiceGrpc.getReportMethod) == null) {
      synchronized (CollectorServiceGrpc.class) {
        if ((getReportMethod = CollectorServiceGrpc.getReportMethod) == null) {
          CollectorServiceGrpc.getReportMethod = getReportMethod =
              io.grpc.MethodDescriptor.<com.lightstep.tracer.grpc.ReportRequest, com.lightstep.tracer.grpc.ReportResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Report"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lightstep.tracer.grpc.ReportRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lightstep.tracer.grpc.ReportResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CollectorServiceMethodDescriptorSupplier("Report"))
              .build();
        }
      }
    }
    return getReportMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CollectorServiceStub newStub(io.grpc.Channel channel) {
    return new CollectorServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CollectorServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CollectorServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CollectorServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CollectorServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class CollectorServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void report(com.lightstep.tracer.grpc.ReportRequest request,
        io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.ReportResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getReportMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getReportMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.lightstep.tracer.grpc.ReportRequest,
                com.lightstep.tracer.grpc.ReportResponse>(
                  this, METHODID_REPORT)))
          .build();
    }
  }

  /**
   */
  public static final class CollectorServiceStub extends io.grpc.stub.AbstractStub<CollectorServiceStub> {
    private CollectorServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CollectorServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CollectorServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CollectorServiceStub(channel, callOptions);
    }

    /**
     */
    public void report(com.lightstep.tracer.grpc.ReportRequest request,
        io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.ReportResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getReportMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class CollectorServiceBlockingStub extends io.grpc.stub.AbstractStub<CollectorServiceBlockingStub> {
    private CollectorServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CollectorServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CollectorServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CollectorServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.lightstep.tracer.grpc.ReportResponse report(com.lightstep.tracer.grpc.ReportRequest request) {
      return blockingUnaryCall(
          getChannel(), getReportMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CollectorServiceFutureStub extends io.grpc.stub.AbstractStub<CollectorServiceFutureStub> {
    private CollectorServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CollectorServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CollectorServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CollectorServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.lightstep.tracer.grpc.ReportResponse> report(
        com.lightstep.tracer.grpc.ReportRequest request) {
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
    private final CollectorServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CollectorServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REPORT:
          serviceImpl.report((com.lightstep.tracer.grpc.ReportRequest) request,
              (io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.ReportResponse>) responseObserver);
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

  private static abstract class CollectorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CollectorServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.lightstep.tracer.grpc.Collector.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CollectorService");
    }
  }

  private static final class CollectorServiceFileDescriptorSupplier
      extends CollectorServiceBaseDescriptorSupplier {
    CollectorServiceFileDescriptorSupplier() {}
  }

  private static final class CollectorServiceMethodDescriptorSupplier
      extends CollectorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CollectorServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (CollectorServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CollectorServiceFileDescriptorSupplier())
              .addMethod(getReportMethod())
              .build();
        }
      }
    }
    return result;
  }
}
