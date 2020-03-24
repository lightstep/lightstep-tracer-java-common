package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpSender extends ProtobufSender {
  private static final MediaType protoMediaType = MediaType.parse("application/octet-stream");

  private final AtomicReference<OkHttpClient> client;
  private final URL collectorURL;
  private final long deadlineMillis;
  private final String accessToken;

  public OkHttpSender(final int deadlineMillis, final String componentName, final String accessToken, final String servicePath, final int servicePort) {
    super(componentName, servicePath, servicePort);
    this.deadlineMillis = deadlineMillis;
    this.accessToken = accessToken;
    this.client = new AtomicReference<>(start(deadlineMillis));
    final int slash = servicePath.indexOf('/');
    if (slash == -1)
      throw new IllegalArgumentException("servicePath (" + servicePath + ") is invalid");

    try {
      this.collectorURL = new URL("https", servicePath.substring(0, slash), servicePort, servicePath.substring(slash));
    }
    catch (final MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  IngestResponse invoke(final IngestRequest.Builder request, final long timeout) throws IOException {
    final Response response = client().newCall(new Request.Builder()
        .url(collectorURL)
        .addHeader("Lightstep-Access-Token", accessToken)
        .addHeader("Accept", "application/octet-stream")
        .addHeader("Content-Type", "application/octet-stream")
        .post(RequestBody.create(protoMediaType, request.build().toByteArray()))
        .build())
      .execute();

    return IngestResponse.parseFrom(response.body().byteStream());
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