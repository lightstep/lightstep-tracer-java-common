package com.lightstep.tracer.shared;

import static com.lightstep.tracer.shared.LightStepConstants.Tags.COMPONENT_NAME_KEY;
import static com.lightstep.tracer.shared.LightStepConstants.Tags.GUID_KEY;
import static com.lightstep.tracer.shared.LightStepConstants.Tags.LEGACY_COMPONENT_NAME_KEY;
import static com.lightstep.tracer.shared.LightStepConstants.Collector.DEFAULT_PLAINTEXT_PORT;
import static com.lightstep.tracer.shared.LightStepConstants.Collector.DEFAULT_SECURE_PORT;
import static com.lightstep.tracer.shared.LightStepConstants.Collector.PATH;
import static com.lightstep.tracer.shared.LightStepConstants.Collector.PROTOCOL_HTTP;
import static com.lightstep.tracer.shared.LightStepConstants.Collector.PROTOCOL_HTTPS;
import static com.lightstep.tracer.shared.Options.BUILTIN_PROPAGATORS;
import static com.lightstep.tracer.shared.Options.VERBOSITY_DEBUG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import io.opentracing.ScopeManager;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMap;
import io.opentracing.util.ThreadLocalScopeManager;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import com.lightstep.tracer.shared.Options.OkHttpDns;

import org.junit.Test;

public class OptionsTest {
    private static final String ACCESS_TOKEN = "my-access-token";
    private static final String SERVICE_VERSION = "v0.1.0";
    private static final String COLLECTOR_HOST = "my-collector-host";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String COMPONENT_NAME = "my-component";
    private static final int MAX_REPORTING_INTERVAL_MILLIS = 1001;
    private static final int MAX_BUFFERED_SPANS = 999;
    private static final String TAG_KEY = "my-tag-key";
    private static final String TAG_VALUE = "my-tag-value";
    private static final long GUID_VALUE = 123;
    private static final long DEADLINE_MILLIS = 150;
    private static final Propagator CUSTOM_PROPAGATOR = new B3Propagator();
    private static final OkHttpDns CUSTOM_DNS = new OkHttpDns(){
        @Override
        public List<InetAddress> lookup(String hostname) {
            return Collections.emptyList();
        }
    };

    /**
     * Basic test of OptionsBuilder that ensures if I set everything explicitly, that these values
     * are propagated to the Options object.
     */
    @Test
    public void testOptionsBuilder() throws Exception {
        Options options = createFullyPopulatedOptions();
        validateFullyPopulatedOptions(options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_invalidProtocol() {
        new Options.OptionsBuilder()
                .withCollectorProtocol("bogus");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_nullCollectorHost() {
        new Options.OptionsBuilder()
                .withCollectorHost(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_whitespaceCollectorHost() {
        new Options.OptionsBuilder()
                .withCollectorHost("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_emptyCollectorHost() {
        new Options.OptionsBuilder()
                .withCollectorHost("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_zeroCollectorPort() {
        new Options.OptionsBuilder()
                .withCollectorPort(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_negativeCollectorPort() {
        new Options.OptionsBuilder()
                .withCollectorPort(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_nullPropagator() {
        new Options.OptionsBuilder()
                .withPropagator(Builtin.TEXT_MAP, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_nullPropagatorFormat() {
        new Options.OptionsBuilder()
                .withPropagator(null, CUSTOM_PROPAGATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_nullScopeManager() {
        new Options.OptionsBuilder()
                .withScopeManager(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_nullOkHttpDns() {
        new Options.OptionsBuilder()
                .withOkHttpDns(null);
    }

    @Test
    public void testOptionsBuilder_defaultAccessToken() throws Exception {
        Options options = new Options.OptionsBuilder()
                .build();
        assertEquals("", options.accessToken);
    }

    @Test
    public void testOptionsBuilder_defaultServiceVersion() throws Exception {
        Options options = new Options.OptionsBuilder()
                .build();
        assertEquals("", options.serviceVersion);
    }

    @Test
    public void testOptionsBuilder_withNullAccessToken() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withAccessToken(null)
                .build();
        assertEquals("", options.accessToken);
    }

    @Test
    public void testOptionsBuilder_httpsNoPortProvided() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withCollectorProtocol(PROTOCOL_HTTPS)
                .build();

        assertEquals(DEFAULT_SECURE_PORT, options.collectorUrl.getPort());
    }

    @Test
    public void testOptionsBuilder_defaultScopeManager() throws Exception {
        Options options = new Options.OptionsBuilder()
                .build();

        assertTrue(options.scopeManager instanceof ThreadLocalScopeManager);
    }

    @Test
    public void testOptionsBuilder_withScopeManager() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withScopeManager(io.opentracing.noop.NoopScopeManager.INSTANCE)
                .build();

        assertTrue(options.scopeManager instanceof io.opentracing.noop.NoopScopeManager);
    }

    @Test
    public void testOptionsBuilder_httpNoPortProvided() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withCollectorProtocol(PROTOCOL_HTTP)
                .build();

        assertEquals(DEFAULT_PLAINTEXT_PORT, options.collectorUrl.getPort());
    }

    @Test
    public void testOptionsBuilder_noOkHttpDnsProvided() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withCollectorProtocol(PROTOCOL_HTTP)
                .build();

        assertNull(options.okhttpDns);
    }

    @Test
    public void testOptionsBuilder_fromExistingOptions() throws Exception {
        // create Options object with values configured
        Options options = createFullyPopulatedOptions();

        // create a new Options object from the other, all the values should be the same
        Options.OptionsBuilder builder = new Options.OptionsBuilder(options);
        Options newOptions = builder.build();
        assertNotSame(newOptions, options);
        validateFullyPopulatedOptions(newOptions);

        // verify that if we change something, it is changed in resulting Options
        Options modifiedOptions = builder.withMaxReportingIntervalMillis(222).build();
        assertEquals(222, modifiedOptions.maxReportingIntervalMillis);
    }

    @Test
    public void testOptionsBuilder_noComponentName() throws Exception {
        Options options = new Options.OptionsBuilder().build();
        Object componentName = options.tags.get(COMPONENT_NAME_KEY);
        assertNotNull(componentName);
    }

    @Test
    public void testOptionsBuilder_noGuid() throws Exception {
        Options options = new Options.OptionsBuilder().build();
        assertNotEquals(0L, options.getGuid());
    }

    @Test
    public void testOptionsBuilder_noFormatsProvided() throws Exception {
        Options options = new Options.OptionsBuilder().build();
        assertFalse(options.propagators.isEmpty());
        assertEquals(BUILTIN_PROPAGATORS, options.propagators);
        assertNotSame(BUILTIN_PROPAGATORS, options.propagators);
    }

    @Test
    public void testSetDefaultReportingIntervalMillis_alreadySet() throws Exception {
        Options oldOptions = createFullyPopulatedOptions();
        Options newOptions = oldOptions.setDefaultReportingIntervalMillis(111);
        assertSame(oldOptions, newOptions);
        assertEquals(MAX_REPORTING_INTERVAL_MILLIS, newOptions.maxReportingIntervalMillis);
    }

    @Test
    public void testSetDefaultReportingIntervalMillis_notSet() throws Exception {
        Options oldOptions = new Options.OptionsBuilder().build();
        Options newOptions = oldOptions.setDefaultReportingIntervalMillis(111);
        assertNotSame(oldOptions, newOptions);
        assertEquals(111, newOptions.maxReportingIntervalMillis);
    }

    @Test
    public void testOptionsBuilder_defaultMetricsUrl() throws Exception {
        Options options = new Options.OptionsBuilder()
                .build();

        assertEquals(LightStepConstants.Metrics.DEFAULT_URL, options.metricsUrl);
    }

    @Test
    public void testOptionsBuilder_defaultDisableMetricsReporting() throws Exception {
        Options options = new Options.OptionsBuilder()
                .build();

        assertFalse(options.disableMetricsReporting);
    }

    @Test
    public void testOptionsBuilder_hostname_notSet() throws Exception {
        Options options = new Options.OptionsBuilder().build();
        assertFalse(options.hostname.isEmpty());
    }

    @Test
    public void testOptionsBuilder_hostname_set() throws Exception {
        Options options = new Options.OptionsBuilder().withHostname("my-host").build();
        assertEquals("my-host", options.hostname);
    }

    private Options createFullyPopulatedOptions() throws Exception {
        return new Options.OptionsBuilder()
                .withVerbosity(VERBOSITY_DEBUG)
                .withAccessToken(ACCESS_TOKEN)
                .withServiceVersion(SERVICE_VERSION)
                .withCollectorPort(123)
                .withCollectorHost(COLLECTOR_HOST)
                .withCollectorProtocol(HTTPS_PROTOCOL)
                .withComponentName(COMPONENT_NAME)
                .withDisableReportingLoop(true)
                .withResetClient(true)
                .withClockSkewCorrection(false)
                .withMaxReportingIntervalMillis(MAX_REPORTING_INTERVAL_MILLIS)
                .withMaxBufferedSpans(MAX_BUFFERED_SPANS)
                .withTag(TAG_KEY, TAG_VALUE)
                .withTag(GUID_KEY, GUID_VALUE)
                .withDeadlineMillis(DEADLINE_MILLIS)
                .withPropagator(Builtin.TEXT_MAP, CUSTOM_PROPAGATOR)
                .withScopeManager(new ThreadLocalScopeManager())
                .withMetricsUrl(COLLECTOR_HOST)
                .withDisableMetricsReporting(true)
                .withDisableMetaEventLogging(true)
                .withOkHttpDns(CUSTOM_DNS)
                .build();
    }

    private void validateFullyPopulatedOptions(Options options) {
        assertEquals(VERBOSITY_DEBUG, options.verbosity);
        assertEquals(ACCESS_TOKEN, options.accessToken);
        assertEquals(SERVICE_VERSION, options.serviceVersion);
        assertEquals(
                "https://my-collector-host:123" + PATH,
                options.collectorUrl.toString()
        );
        assertEquals(COMPONENT_NAME, options.tags.get(COMPONENT_NAME_KEY));
        assertEquals(COMPONENT_NAME, options.getComponentName());
        assertTrue(options.disableReportingLoop);
        assertTrue(options.resetClient);
        assertFalse(options.useClockCorrection);
        assertEquals(MAX_REPORTING_INTERVAL_MILLIS, options.maxReportingIntervalMillis);
        assertEquals(MAX_BUFFERED_SPANS, options.maxBufferedSpans);
        assertEquals(TAG_VALUE, options.tags.get(TAG_KEY));
        assertEquals(GUID_VALUE, options.getGuid());
        assertEquals(DEADLINE_MILLIS, options.deadlineMillis);
        assertFalse(options.propagators.keySet().isEmpty());
        assertEquals(CUSTOM_PROPAGATOR, options.propagators.get(Builtin.TEXT_MAP));
        assertEquals(COLLECTOR_HOST, options.metricsUrl);
        assertTrue(options.disableMetricsReporting);
        assertTrue(options.disableMetaEventLogging);
        assertEquals(CUSTOM_DNS, options.okhttpDns);
    }
}
