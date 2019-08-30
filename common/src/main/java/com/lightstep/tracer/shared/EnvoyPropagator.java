package com.lightstep.tracer.shared;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opentracing.propagation.BinaryExtract;
import io.opentracing.propagation.BinaryInject;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.lightstep.tracer.grpc.propagation.Lightstep.BinaryCarrier;
import static com.lightstep.tracer.grpc.propagation.Lightstep.BasicTracerCarrier;

public class EnvoyPropagator implements Propagator {

    public <C> void inject(SpanContext spanContext, C carrier) {
        if (!(carrier instanceof BinaryInject)) {
            return;
        }

        BasicTracerCarrier.Builder basicCarrierBuilder = BasicTracerCarrier.newBuilder()
            .setTraceId(spanContext.getTraceId())
            .setSpanId(spanContext.getSpanId())
            .setSampled(true);
        for (Map.Entry<String, String> entry: spanContext.baggageItems()) {
            basicCarrierBuilder.putBaggageItems(entry.getKey(), entry.getValue());
        }

        BinaryCarrier bCarrier = BinaryCarrier.newBuilder()
            .setBasicCtx(basicCarrierBuilder.build())
            .build();

        byte[] buff = bCarrier.toByteArray();
        ByteBuffer byteBuff = ((BinaryInject) carrier).injectionBuffer(buff.length);
        byteBuff.put(buff);
    }

    @Override
    public <C> SpanContext extract(C carrier) {
        if (!(carrier instanceof BinaryExtract)) {
            return null;
        }

        ByteBuffer byteBuff = ((BinaryExtract) carrier).extractionBuffer();
        if (byteBuff == null || !byteBuff.hasRemaining()) {
            return null;
        }

        BasicTracerCarrier basicCarrier = null;
        try {
            basicCarrier = BinaryCarrier.parseFrom(byteBuff).getBasicCtx();
        } catch (InvalidProtocolBufferException e) {
            return null;
        }

        Map<String, String> baggage = new HashMap<>();
        for (Map.Entry<String, String> entry: basicCarrier.getBaggageItemsMap().entrySet()) {
            baggage.put(entry.getKey(), entry.getValue());
        }

        return new SpanContext(basicCarrier.getTraceId(), basicCarrier.getSpanId(), baggage);
    }
}
