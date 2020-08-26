package com.lightstep.tracer.shared;

public final class LightStepConstants {
    private LightStepConstants() {}

    public final class Collector {
      private Collector() {}

      /**
       * Hostname that will be used for the collector if no other value is provided.
       */
      static final String DEFAULT_HOST = "collector-grpc.lightstep.com";

      static final String PATH = "/api/v2/reports";

      /**
       * Default collector port for HTTPS
       */
      static final int DEFAULT_SECURE_PORT = 443;

      /**
       * Default collector port for HTTP
       */
      static final int DEFAULT_PLAINTEXT_PORT = 80;

      public static final String PROTOCOL_HTTPS = "https";
      public static final String PROTOCOL_HTTP = "http";
    }

    public final class Metrics {
        private Metrics() {
        }

        static final int DEFAULT_INTERVAL_SECS = 30;
        static final String DEFAULT_URL = "https://ingest.lightstep.com:443/metrics";
        static final String LS_METRICS_ENABLED = "LS_METRICS_ENABLED";
        static final boolean DEFAULT_DISABLE_METRICS = false;
    }

    public final class Tags {
        private Tags() {}

        public static final String LEGACY_COMPONENT_NAME_KEY = "component_name";
        public static final String COMPONENT_NAME_KEY = "lightstep.component_name";
        public static final String GUID_KEY = "lightstep.guid";
    }

    public final class MetaEvents {
        private MetaEvents() {}

        public static final String MetaEventKey = "lightstep.meta_event";
        public static final String PropagationFormatKey = "lightstep.propagation_format";
        public static final String TraceIdKey = "lightstep.trace_id";
        public static final String SpanIdKey = "lightstep.span_id";
        public static final String TracerGuidKey = "lightstep.tracer_guid";
        public static final String ExtractOperation = "lightstep.extract_span";
        public static final String InjectOperation = "lightstep.inject_span";
        public static final String SpanStartOperation = "lightstep.span_start";
        public static final String SpanFinishOperation = "lightstep.span_finish";
        public static final String TracerCreateOperation = "lightstep.tracer_create";
    }

    final class Internal {
        private Internal() {}

        static final String REPORTING_THREAD_NAME = "lightstep-reporting-thread";
    }
}
