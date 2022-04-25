package com.lightstep.tracer.shared;

import io.opentracing.Span;

import java.util.Random;

public class Main {
    public static void main(String... args) throws Exception {
        ExampleTracer tracer = new ExampleTracer(new Options.OptionsBuilder()
                .withCollectorHost(System.getenv("_PLATFORM_OBSERVABILITY_COLLECTOR_ENDPOINT"))
                .withCollectorPort(8443)
                .withVerbosity(3)
                .withComponentName("java-trace-spammer")
                .withAccessToken(System.getenv("_PLATFORM_OBSERVABILITY_ACCESS_TOKEN"))
                .build()
        );

        while (true){
            for(int i=0;i<1000;i++){
                Span span = tracer.buildSpan(String.format("test-span-%d", i)).start();
                span.setTag("tag-name", "tag-value");
                span.setTag("tag-name1", "tag-value1");
                span.setBaggageItem("baggage-item1", "baggage-item-value1");
                Thread.sleep(3);
                span.finish();
            }

            tracer.flushInternal(true);
        }
    }
}
