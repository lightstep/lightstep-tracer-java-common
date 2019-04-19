package com.lightstep.tracer.grpc;

import io.grpc.*;
import static io.grpc.Metadata.*;

public class GrpcClientInterceptor implements ClientInterceptor {
    public static final Key<String> ACCESS_TOKEN_HEADER = Key.of("Lightstep-Access-Token", ASCII_STRING_MARSHALLER);

    private String authToken;

    public GrpcClientInterceptor(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> methodDescriptor,
            CallOptions callOptions,
            Channel channel
    ) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                channel.newCall(methodDescriptor, callOptions)
        ) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers){
                headers.put(ACCESS_TOKEN_HEADER, authToken);
                super.start(responseListener, headers);
            }
        };
    }
}
