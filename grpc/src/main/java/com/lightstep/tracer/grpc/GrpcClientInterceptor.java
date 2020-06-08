package com.lightstep.tracer.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class GrpcClientInterceptor implements ClientInterceptor {
    public static final Key<String> ACCESS_TOKEN_HEADER = Key.of("Lightstep-Access-Token", ASCII_STRING_MARSHALLER);

    private String authToken;
    private Map<Metadata.Key<String>, String> customHeaders;

    public GrpcClientInterceptor(String authToken, Map<String, String> headers) {
        this.authToken = authToken;
        if (headers != null && !headers.isEmpty()) {
            this.customHeaders = new HashMap<>();
            for (Entry<String, String> entry : headers.entrySet()) {
                this.customHeaders.put(Key.of(entry.getKey(), ASCII_STRING_MARSHALLER),
                    entry.getValue());
            }
        }
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
                if (customHeaders != null) {
                    for (Entry<Key<String>, String> entry : customHeaders.entrySet()) {
                        headers.put(entry.getKey(), entry.getValue());
                    }
                }

                super.start(responseListener, headers);
            }
        };
    }
}
