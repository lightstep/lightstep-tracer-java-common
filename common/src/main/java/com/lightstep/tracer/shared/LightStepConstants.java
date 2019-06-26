package com.lightstep.tracer.shared;

public final class LightStepConstants {
    private LightStepConstants() {}

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
