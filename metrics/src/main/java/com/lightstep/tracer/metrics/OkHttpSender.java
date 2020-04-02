package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkHttpSender extends ProtobufSender {
  // TODO - unify constants with the main artifact.
  private static final String OCTET_STREAM_TYPE = "application/octet-stream";
  private static final MediaType protoMediaType = MediaType.parse(OCTET_STREAM_TYPE);

  private final AtomicReference<OkHttpClient> client;
  private final URL collectorURL;
  private final long deadlineMillis;

  public OkHttpSender(final String componentName, final String accessToken, final String serviceUrl, final int deadlineMillis, final boolean sendFirstReport) {
    super(componentName, accessToken, serviceUrl, sendFirstReport);
    this.deadlineMillis = deadlineMillis;
    this.client = new AtomicReference<>(start(deadlineMillis));

    try {
      this.collectorURL = new URL(serviceUrl);
    }
    catch (final MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  IngestResponse invoke(final IngestRequest.Builder request, final long timeout) throws IOException {
    final Call call = client().newCall(new Request.Builder()
        .url(collectorURL)
        .addHeader("Accept", OCTET_STREAM_TYPE)
        .addHeader("Content-Type", OCTET_STREAM_TYPE)
        .addHeader("Lightstep-Access-Token", accessToken)
        .post(RequestBody.create(protoMediaType, request.build().toByteArray()))
      .build());

    call.timeout().deadline(timeout, TimeUnit.MILLISECONDS);
    return IngestResponse.parseFrom(call.execute().body().byteStream());
  }

  private OkHttpClient client() {
    return client.get();
  }

  private void reconnect() {
    close();
    client.set(start(deadlineMillis));
  }

  private static OkHttpClient start(final long deadlineMillis) {
    return new OkHttpClient.Builder().connectTimeout(deadlineMillis, TimeUnit.MILLISECONDS).build();
  }

  @Override
  public void close() {
    final OkHttpClient client = client();
    if (client != null)
      client.dispatcher().executorService().shutdown();
  }
}
