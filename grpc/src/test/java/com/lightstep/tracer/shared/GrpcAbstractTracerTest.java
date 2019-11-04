package com.lightstep.tracer.shared;

import io.opentracing.propagation.BinaryAdapters;
import java.nio.ByteBuffer;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import static com.lightstep.tracer.shared.Options.VERBOSITY_DEBUG;
import static com.lightstep.tracer.shared.Options.VERBOSITY_ERRORS_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_FIRST_ERROR_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_INFO;
import static com.lightstep.tracer.shared.Options.VERBOSITY_NONE;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class GrpcAbstractTracerTest {

    private static final String ACCESS_TOKEN = "abc123";
    private static final String TEST_MSG = "hello tracer";
    private static final long TRACE_ID = 1;
    private static final long SPAN_ID = 2;
    private static final String BAGGAGE_KEY = "baggage-key";
    private static final String BAGGAGE_VALUE = "baggage-value";

    @Mock
    private SpanContext invalidSpanContext;

    @Mock
    private TextMap textMap;

    @Mock
    private TextMap httpHeaders;

    @Mock
    private ByteBuffer byteBuffer;

    @Mock
    private Format<Object> genericFormat;

    private StubTracer undertest;

    private com.lightstep.tracer.shared.SpanContext spanContext;

    @Before
    public void setup() {
        Map<String, String> baggage = Collections.singletonMap(BAGGAGE_KEY, BAGGAGE_VALUE);
        spanContext = new com.lightstep.tracer.shared.SpanContext(TRACE_ID, SPAN_ID, baggage);
    }

    @After
    public void teardown() {
        if (undertest != null) {
            undertest.close();
        }
    }

    /**
     * Provides an implementation of AbstractTracer (for use in testing) that is configured to
     * the provided verbosity level.
     */
    private StubTracer createTracer(int verbosity) throws Exception {
        Options options = new Options.OptionsBuilder()
                .withAccessToken(ACCESS_TOKEN)
                .withVerbosity(verbosity)
                .build();
        return createTracer(options);
    }

    /**
     * Provides an implementation of AbstractTracer (for use in testing) that is configured with
     * the provided Options.
     */
    private StubTracer createTracer(Options options) {
        return new StubTracer(options);
    }

    @Test
    public void testVerbosityDebug() throws Exception {
        undertest = createTracer(VERBOSITY_DEBUG);
        callAllLogMethods(undertest);

        // At debug level, all of the calls should be logged
        assertEquals(4, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityInfo() throws Exception {
        undertest = createTracer(VERBOSITY_INFO);
        callAllLogMethods(undertest);

        // At info level, all of the calls should be logged, except the debug log
        assertEquals(3, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityErrorsOnly() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        callAllLogMethods(undertest);
        undertest.error(TEST_MSG); // make an extra call to error, all error calls should be logged

        // At error level, all of the error calls should be logged
        assertEquals(2, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityNone() throws Exception {
        undertest = createTracer(VERBOSITY_NONE);
        callAllLogMethods(undertest);

        // At none, nothing should be logged
        assertTrue(undertest.consoleLogCallsIsEmpty());
    }

    @Test
    public void testVerbosityFirstErrorOnly() throws Exception {
        undertest = createTracer(VERBOSITY_FIRST_ERROR_ONLY);
        callAllLogMethods(undertest);
        undertest.error(TEST_MSG); // make a second call to error

        // At error level, only the first error call should be logged
        assertEquals(1, undertest.getNumberOfConsoleLogCalls());
    }

    /**
     * Calls each of the available logging methods on the tracer with a dummy message.
     */
    private void callAllLogMethods(StubTracer undertest) {
        undertest.debug(TEST_MSG);
        undertest.info(TEST_MSG);
        undertest.warn(TEST_MSG);
        undertest.error(TEST_MSG);
    }

    @Test
    public void testFlush_timeoutOccurs() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.flushResult = new SimpleFuture<>();
        assertNull(undertest.flush(1L));
    }

    @Test
    public void testFlush_noTimeoutSuccess() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.flushResult = new SimpleFuture<>(true);
        assertTrue(undertest.flush(20000L));
    }

    @Test
    public void testFlush_noTimeoutFailure() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.flushResult = new SimpleFuture<>(false);
        assertFalse(undertest.flush(20000L));
    }

    @Test
    public void testGenerateTraceURL() throws Exception {
        long spanId = 3;
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        String result = undertest.generateTraceURL(spanId);
        String expectedUrlStart = "https://app.lightstep.com/" + ACCESS_TOKEN + "/trace?span_guid="
                + Long.toHexString(spanId) + "&at_micros=";
        assertTrue("Unexpected trace url: " + result, result.startsWith(expectedUrlStart));
    }

    /**
     * Ensures that if an invalid spanContext is passed to inject, that no ClassCastException is
     * thrown, instead an error is logged and the carrier injection is skipped.
     */
    @Test
    public void testInject_invalidSpanContextType() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.inject(invalidSpanContext, Format.Builtin.TEXT_MAP, textMap);
        verifyZeroInteractions(textMap);
    }

    @Test
    public void testInject_textMap() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.inject(spanContext, Format.Builtin.TEXT_MAP, textMap);
        verify(textMap).put(TextMapPropagator.FIELD_NAME_TRACE_ID, Long.toHexString(TRACE_ID));
        verify(textMap).put(TextMapPropagator.FIELD_NAME_SPAN_ID, Long.toHexString(SPAN_ID));
        verify(textMap).put(TextMapPropagator.FIELD_NAME_SAMPLED, "true");
        verify(textMap).put(TextMapPropagator.PREFIX_BAGGAGE + BAGGAGE_KEY, BAGGAGE_VALUE);
    }

    @Test
    public void testInject_httpHeaders() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.inject(spanContext, Format.Builtin.HTTP_HEADERS, httpHeaders);
        verify(httpHeaders).put(TextMapPropagator.FIELD_NAME_TRACE_ID, Long.toHexString(TRACE_ID));
        verify(httpHeaders).put(TextMapPropagator.FIELD_NAME_SPAN_ID, Long.toHexString(SPAN_ID));
        verify(httpHeaders).put(TextMapPropagator.FIELD_NAME_SAMPLED, "true");
        verify(httpHeaders).put(TextMapPropagator.PREFIX_BAGGAGE + BAGGAGE_KEY, BAGGAGE_VALUE);
    }

    @Test
    public void testInject_binary() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.inject(spanContext, Format.Builtin.BINARY_INJECT, BinaryAdapters.injectionCarrier(byteBuffer));
        verifyZeroInteractions(byteBuffer);
    }

    /**
     * Ensures that no exception are thrown if an unsupported Format type is provided.
     */
    @Test
    public void testInject_unsupportedFormat() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.inject(spanContext, genericFormat, textMap);
        verifyZeroInteractions(textMap);
    }

    @Test
    public void testSpanContext_TraceId() throws Exception {
        undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        Span span = (Span) undertest.buildSpan("traceId").start();
        io.opentracing.Scope scope = undertest.activateSpan(span);
        assertEquals(Util.toHexString(span.context().getTraceId()), span.context().toTraceId());
        assertEquals(Util.toHexString(span.context().getSpanId()), span.context().toSpanId());
        scope.close();
    }
}
