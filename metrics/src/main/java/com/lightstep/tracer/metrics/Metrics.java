package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstep.tracer.retry.ExponentialBackoffRetryPolicy;
import com.lightstep.tracer.retry.RetryFailureException;
import com.lightstep.tracer.retry.RetryPolicy;
import com.lightstep.tracer.retry.Retryable;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class Metrics extends Thread implements Retryable<Void>, AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(Metrics.class);
  private static final boolean isJdk17 = System.getProperty("java.version").equals("1.7");
  private static volatile boolean inited;
  private static Metrics instance;

  public static Metrics getInstance(final String componentName, final int samplePeriodSeconds, final String hostName, final int port) {
    if (isJdk17) {
      logger.warn("Metrics supports jdk1.8+");
      return null;
    }

    if (inited)
      return instance;

    synchronized (Metrics.class) {
      if (inited)
        return instance;

      inited = true;
      return instance = new Metrics(componentName, samplePeriodSeconds, hostName, port);
    }
  }

  private static final int attempts = Integer.MAX_VALUE;
  private static final int startDelay = 1000;
  private static final int factor = 2;
  private static final int maxDelay = Integer.MAX_VALUE;

  private final ExponentialBackoffRetryPolicy retryPolicy = new ExponentialBackoffRetryPolicy(attempts, startDelay, factor, maxDelay, true, 1) {
    private static final long serialVersionUID = 7311364828386985449L;

    @Override
    protected boolean retryOn(final Exception e) {
      return true;
    }
  };

  private final HardwareAbstractionLayer hal = new SystemInfo().getHardware();
  private final MetricGroup[] metricGroups = {new CpuMetricGroup(hal), new NetworkMetricGroup(hal), new MemoryMetricGroup(hal), new GcMetricGroup(hal)};

  private final int samplePeriodSeconds;
  private final ProtobufSender sender;
  private boolean closed;

  private Metrics(final String componentName, final int samplePeriodSeconds, final String hostName, final int port) {
    if (samplePeriodSeconds < 1)
      throw new IllegalArgumentException("samplePeriodSeconds (" + samplePeriodSeconds + ") < 1");

    this.samplePeriodSeconds = samplePeriodSeconds;
    this.sender = new ProtobufSender(componentName, hostName, port);
  }

  @Override
  public void run() {
    try {
      Thread thread = null;
      while (!closed) {
        sender.updateSampleRequest(metricGroups);
        if (thread != null && thread.isAlive())
          throw new IllegalStateException("Thread should have self-terminated by now: " + (finishBy - System.currentTimeMillis()));

        thread = new Thread() {
          @Override
          public void run() {
            try {
              finishBy = (sender.getPreviousTimestamp() + samplePeriodSeconds) * 1000;
              retryPolicy.run(Metrics.this, finishBy - System.currentTimeMillis());
            }
            catch (final RetryFailureException e) {
              e.printStackTrace();
            }
          }
        };

        thread.start();

        try {
          sleep(samplePeriodSeconds * 1000);
        }
        catch (final InterruptedException e) {
          return;
        }
      }
    }
    catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
    finally {
      synchronized (this) {
        notify();
      }
    }
  }

  private long finishBy;

  @Override
  public Void retry(final RetryPolicy retryPolicy, final int attemptNo) throws Exception {
    final long timeout = finishBy - System.currentTimeMillis();
    if (timeout < 0)
      throw new RetryFailureException(attemptNo, retryPolicy.getDelayMs(attemptNo - 1));

    sender.exec(timeout);
    return null;
  }

  @Override
  public synchronized void start() {
    if (!isAlive())
      super.start();
  }

  @Override
  public void close() throws InterruptedException {
    closed = true;
    interrupt();
    sender.close();
    if (isAlive()) {
      synchronized (this) {
        wait();
      }
    }

    inited = false;
  }
}