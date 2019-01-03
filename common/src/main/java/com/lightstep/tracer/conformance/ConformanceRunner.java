package com.lightstep.tracer.conformance;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.lightstep.tracer.shared.AbstractTracer;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.shared.SimpleFuture;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;

import io.opentracing.propagation.TextMapInjectAdapter;

class ExampleTracer extends AbstractTracer {
    public ExampleTracer(Options options) {
        super(options);
    }

    @Override
    // Flush any data stored in the log and span buffers
    protected SimpleFuture<Boolean> flushInternal(boolean explicitRequest) {
        return new SimpleFuture<>(sendReport(explicitRequest));
    }

    @Override
    protected void printLogToConsole(InternalLogLevel level, String msg, Object payload) {
        String s = msg;
        if (payload != null) {
            s += " " + payload.toString();
        }
        System.out.println(s);
    }
}

class Carriers {
    public Map<String, String> httpHeaders;
    public String binary;

    Carriers() {
        this.httpHeaders = new HashMap<>();
    }

    void setBinary(ByteBuffer buf) {
        this.binary = Base64.getEncoder().encodeToString(buf.array());
    }
}

class Client {
    private Gson gson;
    private Tracer tracer;
    private InputStreamReader input;
    private Carriers outputCarriers;

    Client(Tracer tracer, InputStream in){
        this.tracer= tracer;
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        this.input = new InputStreamReader(in);
        this.outputCarriers = new Carriers();
    };

    void run() {
        Carriers carriers = this.gson.fromJson(new JsonReader(this.input), Carriers.class);
        this.httpHeaders(carriers);
        this.binary(carriers);
        System.out.println(this.gson.toJson(this.outputCarriers));
        System.err.println(this.outputCarriers.binary);
    }
    void httpHeaders(Carriers carriers) {
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(carriers.httpHeaders));

        tracer.inject(context, Format.Builtin.TEXT_MAP, new TextMapInjectAdapter(this.outputCarriers.httpHeaders));
    }
    void binary(Carriers carriers) {
        byte[] decodedBytes = Base64.getDecoder().decode(carriers.binary);

        SpanContext context = tracer.extract(Format.Builtin.BINARY, ByteBuffer.wrap(decodedBytes));

        ByteBuffer out = ByteBuffer.allocate(4096);
        tracer.inject(context, Format.Builtin.BINARY, out);
        this.outputCarriers.setBinary(out);
    }
}

public class ConformanceRunner {
    public static void main(String[] args) throws Exception {
        Tracer tracer = new ExampleTracer(new Options.OptionsBuilder().withAccessToken("invalid").build());
        Client client = new Client(tracer, System.in);
        client.run();
    }
}
