package com.lightstep.tracer.metrics;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.MetricPoint;

public class MetricsTest {
  private static final int countsPerSample = 11;
  private static final String componentName = "test";
  private static final int servicePort = 8851;
  private static final String serviceUrl = "localhost:" + servicePort;
  private static final String[] pointNames = {"cpu.sys", "cpu.total", "cpu.usage", "cpu.user", "mem.available", "mem.total", "net.bytes_recv", "net.bytes_sent", "runtime.java.gc.count", "runtime.java.gc.time", "runtime.java.heap_size"};

  private static void assertMetric(final int expectedCount, final IngestRequest request) {
    assertEquals(expectedCount, request.getPointsCount());
    for (final MetricPoint point : request.getPointsList())
      assertNotEquals(-1, Arrays.binarySearch(pointNames, point.getMetricName()));
  }

  @Test
  public void testSamplePeriod() throws Exception {
    final int samplePeriod = 2;
    final AtomicInteger counter = new AtomicInteger();
    final String[] id = new String[1];
    try (
      final TestServer server = new TestServer(servicePort, req -> {
        assertEquals(id[0] = (id[0] == null ? req.getIdempotencyKey() : id[0]), req.getIdempotencyKey());
      }, (req,res) -> {
        assertMetric(countsPerSample, req);
        counter.getAndIncrement();
        id[0] = null;
      });
      final Metrics metrics = new Metrics(new GrpcSender(componentName, null, serviceUrl, true), samplePeriod);
    ) {
      server.start();
      metrics.start();
      Thread.sleep(2 * samplePeriod * 1000 + 500);
      assertEquals(3, counter.get());
    }
  }

  private class IdTest {
    private boolean shouldBeEqual;
    private String id;
    private int count;
    private boolean hasError;

    private void assertIds(final IngestRequest req) {
      System.out.println("IdTest.assertIds(" + req.getIdempotencyKey() + "[" + req.getPointsCount() + "])");
      if (count == 0)
        count = req.getPointsCount();

      if (id == null) {
        id = req.getIdempotencyKey();
      }
      else {
        // If `shouldBeEqual`, then we're expecting the same request object...
        // But, what if a sample period has lapsed?
        // In this case, the same object should now have a new id cause it has more data.
        if (shouldBeEqual)
          shouldBeEqual = count == req.getPointsCount();

        count = req.getPointsCount();
        assertFalse(id + "[" + count + "] should " + (shouldBeEqual ? "" : "not ") + "be equal to " + req.getIdempotencyKey() + "[" + req.getPointsCount() + "]", hasError |= shouldBeEqual != id.equals(req.getIdempotencyKey()));
      }

      shouldBeEqual = true;
      id = req.getIdempotencyKey();
    }

    private void reset() {
      System.out.println("IdTest.reset()");
      count = 0;
      shouldBeEqual = false;
    }
  }

  @Test
  public void testRetryWithinSamplePeriod() throws Exception {
    final int samplePeriod = 5;
    final AtomicInteger counter = new AtomicInteger();

    // Set both expected point counts to `countsPerSample`, because the server will consume requests within the samplePeriod time.
    final int[] expectedPointCounts = {countsPerSample, countsPerSample};
    final IdTest idTest = new IdTest();
    try (
      final Metrics metrics = new Metrics(new GrpcSender(componentName, null, serviceUrl, true), samplePeriod);
      final TestServer server = new TestServer(servicePort, req -> {
        idTest.assertIds(req);
      }, (req,res) -> {
        assertMetric(expectedPointCounts[counter.getAndIncrement()], req);
        idTest.reset();
      });
    ) {
      // 1. Start the metrics engine, but the server is off.
      metrics.start();

      // 2. Sleep for 2 seconds, allowing the metrics engine to engage its retry mechanism.
      Thread.sleep(2000);

      // 3. Start the server.
      server.start();

      Thread.sleep(3000);
      assertEquals(1, counter.get());

      Thread.sleep(5000);
      assertEquals(2, counter.get());

      assertFalse(idTest.hasError);
    }
  }

  @Test
  public void testRetryBeyondOneSamplePeriod() throws Exception {
    final int samplePeriod = 5;
    final AtomicInteger counter = new AtomicInteger();

    // Set first expected point count to 2x `countsPerSample`, because the first request is not delivered in time until samplePeriod laps.
    // Set second expected point count to `countsPerSample`, because the server will be running by then, and subsequent requests will be delivered.
    final int[] expectedPointCounts = {2 * countsPerSample, countsPerSample};
    final IdTest idTest = new IdTest();
    try (
      final Metrics metrics = new Metrics(new GrpcSender(componentName, null, serviceUrl, true), samplePeriod);
      final TestServer server = new TestServer(servicePort, req -> {
        idTest.assertIds(req);
      }, (req,res) -> {
        assertMetric(expectedPointCounts[counter.getAndIncrement()], req);
        idTest.reset();
      });
    ) {
      // 1. Start the metrics engine, but the server is off.
      metrics.start();

      // 2. Sleep for 6 seconds, allowing the metrics engine to engage its retry mechanism.
      Thread.sleep(6000);

      // 3. Start the server.
      server.start();

      Thread.sleep(3000 + 100);
      assertEquals(1, counter.get());

      Thread.sleep(5000);
      assertEquals(2, counter.get());
    }
  }

  @Test
  public void testRetryBeyondTwoSamplePeriods() throws Exception {
    final int samplePeriod = 4;
    final AtomicInteger counter = new AtomicInteger();

    // Set first expected point count to 3x `countsPerSample`, because the first and second requests are not delivered in time until samplePeriod laps.
    // Set second expected point count to `countsPerSample`, because the server will be running by then, and subsequent requests will be delivered.
    final int[] expectedPointCounts = {3 * countsPerSample, countsPerSample};
    final IdTest idTest = new IdTest();
    try (
      final Metrics metrics = new Metrics(new GrpcSender(componentName, null, serviceUrl, true), samplePeriod);
      final TestServer server = new TestServer(servicePort, req -> {
        idTest.assertIds(req);
      }, (req,res) -> {
        assertMetric(expectedPointCounts[counter.getAndIncrement()], req);
        idTest.reset();
      });
    ) {
      // 1. Start the metrics engine, but the server is off.
      metrics.start();

      // 2. Sleep for 8 seconds, allowing the metrics engine to engage its retry mechanism, leading to 1 message failing.
      Thread.sleep(8000);

      // 3. Start the server.
      server.start();

      Thread.sleep(4000);
      assertEquals(1, counter.get());

      Thread.sleep(4000);
      assertEquals(2, counter.get());
    }
  }
}
