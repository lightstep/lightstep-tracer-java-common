package com.lightstep.tracer.shared;

import io.opentracing.Span;

public class Main {
    public static void main(String... args) throws Exception {
        ExampleTracer tracer = new ExampleTracer(new Options.OptionsBuilder()
                .withAccessToken("YOUR_ACCESS_TOKEN")
                .build()
        );

        Span span = tracer.buildSpan("parent span").startManual();

        Thread.sleep(1000);

        span.finish();

        tracer.flushInternal(true);
    }
}
