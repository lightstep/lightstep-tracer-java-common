package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;
import com.lightstep.tracer.shared.Options.OkHttpDns;

import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class HttpCollectorClient extends CollectorClient {
    private static final MediaType protoMediaType = MediaType.parse("application/octet-stream");

    private final AtomicReference<OkHttpClient> client;
    private final AbstractTracer tracer;
    private final URL collectorURL;
    private final long deadlineMillis;
    private final OkHttpDns dns;

    HttpCollectorClient(
            AbstractTracer tracer,
            URL collectorURL,
            long deadlineMillis,
            OkHttpDns dns
    ) {
        this.client = new AtomicReference<>(start(deadlineMillis, dns));
        this.tracer = tracer;
        this.collectorURL = collectorURL;
        this.deadlineMillis = deadlineMillis;
        this.dns = dns;
    }

    @Override
    ReportResponse report(ReportRequest request) {
        try {
            return fromResponse(this.client().newCall(toRequest(request)).execute());
        } catch (Exception e) {
            tracer.error("Exception sending report to collector: ", e);
            return null;
        }
    }

    @Override
    void reconnect() {
        shutdown(this.client.getAndSet(start(this.deadlineMillis, this.dns)));
    }

    @Override
    void shutdown() {
        shutdown(this.client());
    }

    private OkHttpClient client() {
        return this.client.get();
    }

    private Request toRequest(ReportRequest request) {
        return new Request.Builder()
                .url(this.collectorURL)
                .post(RequestBody.create(protoMediaType, request.toByteArray()))
                .addHeader("Lightstep-Access-Token", request.getAuth().getAccessToken())
                .build();
    }

    private ReportResponse fromResponse(Response response) throws IOException {
        try {
            if (!response.isSuccessful()) {
                this.tracer.error(String.format(
                        "Collector returned non-successful http code %d, message: %s",
                        response.code(), response.message()
                ));
                return null;
            }

            return fromResponseBody(response.body());
        } finally {
            response.close();
        }
    }

    private ReportResponse fromResponseBody(ResponseBody body) throws IOException {
        if (body == null) {
            this.tracer.error("Collector returned an empty body");
            return null;
        }
        return ReportResponse.parseFrom(body.byteStream());
    }

    private static void shutdown(OkHttpClient client) {
        client.dispatcher().executorService().shutdown();
    }

    private static OkHttpClient start(long deadlineMillis, OkHttpDns dns) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(deadlineMillis, TimeUnit.MILLISECONDS);

        if (dns != null) {
            builder.dns(new CustomDns(dns));
        }

        return builder.build();
    }

    static class CustomDns implements Dns {
        final Options.OkHttpDns dns;

        public CustomDns(Options.OkHttpDns dns) {
            this.dns = dns;
        }

        @Override
        public List<InetAddress> lookup(String hostname) {
            return dns.lookup(hostname);
        }
    }
}
