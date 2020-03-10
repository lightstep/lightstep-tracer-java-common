package com.lightstep.tracer.metrics;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class MetricsTest {
  private static final String componentName = "test";
  private static final String hostName = "localhost";
  private static final int port = 8851;

  @Test
  public void testSamplePeriod() throws Exception {
    final int samplePeriod = 2;
    final AtomicInteger counter = new AtomicInteger();
    final String[] id = new String[1];
    try (
      final TestServer server = new TestServer(port, req -> {
        assertEquals(id[0] = (id[0] == null ? req.getIdempotencyKey() : id[0]), req.getIdempotencyKey());
      }, (req,res) -> {
        assertEquals(10, req.getPointsCount());
        counter.getAndIncrement();
        id[0] = null;
      });
      final Metrics metrics = Metrics.getInstance(componentName, samplePeriod, hostName, port);
    ) {
      server.start();
      metrics.start();
      Thread.sleep(2 * samplePeriod * 1000 + 500);
      assertEquals(3, counter.get());
    }
  }

  @Test
  public void testRetryWithinSamplePeriod() throws Exception {
    final int samplePeriod = 5;
    final AtomicInteger counter = new AtomicInteger();

    // Set both expected point counts to 10, because the server will consume requests within the samplePeriod time.
    final int[] expectedPointCounts = {10, 10};
    final String[] id = new String[1];
    try (
      final Metrics metrics = Metrics.getInstance(componentName, samplePeriod, hostName, port);
      final TestServer server = new TestServer(port, req -> {
        assertEquals(id[0] = (id[0] == null ? req.getIdempotencyKey() : id[0]), req.getIdempotencyKey());
      }, (req,res) -> {
        assertEquals(expectedPointCounts[counter.getAndIncrement()], req.getPointsCount());
        id[0] = null;
      });
    ) {
      // 1. Start the metrics engine, but the server is off.
      metrics.start();

      // 2. Sleep for 4 seconds, allowing the metrics engine to engage its retry mechanism.
      Thread.sleep(2000);

      // 3. Start the server.
      server.start();

      Thread.sleep(3000);
      assertEquals(1, counter.get());

      Thread.sleep(5000);
      assertEquals(2, counter.get());
    }
  }

  @Test
  public void testRetryBeyondOneSamplePeriod() throws Exception {
    final int samplePeriod = 5;
    final AtomicInteger counter = new AtomicInteger();

    // Set first expected point count to 20, because the first request is not delivered in time until samplePeriod laps.
    // Set second expected point count to 10, because the server will be running by then, and subsequent requests will be delivered.
    final int[] expectedPointCounts = {20, 10};
    final String[] id = new String[1];
    try (
      final Metrics metrics = Metrics.getInstance(componentName, samplePeriod, hostName, port);
      final TestServer server = new TestServer(port, req -> {
        assertEquals(id[0] = (id[0] == null ? req.getIdempotencyKey() : id[0]), req.getIdempotencyKey());
      }, (req,res) -> {
        assertEquals(expectedPointCounts[counter.getAndIncrement()], req.getPointsCount());
        id[0] = null;
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

    // Set first expected point count to 30, because the first and second requests are not delivered in time until samplePeriod laps.
    // Set second expected point count to 10, because the server will be running by then, and subsequent requests will be delivered.
    final int[] expectedPointCounts = {30, 10};
    final String[] id = new String[1];
    try (
      final Metrics metrics = Metrics.getInstance(componentName, samplePeriod, hostName, port);
      final TestServer server = new TestServer(port, req -> {
        assertEquals(id[0] = (id[0] == null ? req.getIdempotencyKey() : id[0]), req.getIdempotencyKey());
      }, (req,res) -> {
        assertEquals(expectedPointCounts[counter.getAndIncrement()], req.getPointsCount());
        id[0] = null;
      });
    ) {
      // 1. Start the metrics engine, but the server is off.
      metrics.start();

      // 2. Sleep for 9 seconds, allowing the metrics engine to engage its retry mechanism, leading to 1 message failing.
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