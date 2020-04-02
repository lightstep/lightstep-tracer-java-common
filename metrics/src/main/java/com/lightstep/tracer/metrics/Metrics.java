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

  private static final int attempts = Integer.MAX_VALUE;
  private static final int startDelay = 1000;
  private static final int factor = 2;
  private static final int maxDelay = Integer.MAX_VALUE;

  private static final ExponentialBackoffRetryPolicy retryPolicy = new ExponentialBackoffRetryPolicy(attempts, startDelay, factor, maxDelay, true, 1) {
    private static final long serialVersionUID = 7311364828386985449L;

    @Override
    protected boolean retryOn(final Exception e) {
      if (logger.isDebugEnabled())
        logger.warn(e.getMessage(), e);
      else
        logger.warn(e.getClass().getName() + ": " + e.getMessage());

      return true;
    }
  };

  private final HardwareAbstractionLayer hal = new SystemInfo().getHardware();
  private final MetricGroup[] metricGroups = {new CpuMetricGroup(hal), new NetworkMetricGroup(hal), new MemoryMetricGroup(hal), new GcMetricGroup(hal)};

  private final int samplePeriodSeconds;
  private final Sender<?,?> sender;
  private boolean closed;

  public Metrics(final Sender<?,?> sender, final int samplePeriodSeconds) {
    if (samplePeriodSeconds < 1)
      throw new IllegalArgumentException("samplePeriodSeconds (" + samplePeriodSeconds + ") < 1");

    this.samplePeriodSeconds = samplePeriodSeconds;
    this.sender = sender;
  }

  private static String stackTraceToString(final StackTraceElement[] elements) {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < elements.length; ++i) {
      if (i > 0)
        builder.append('\n');

      builder.append("  ").append(elements[i].toString());
    }

    return builder.toString();
  }

  @Override
  public void run() {
    try {
      Thread thread = null;
      while (!closed) {
        sender.updateSampleRequest(metricGroups);
        if (thread != null && thread.isAlive()) {
          final String message = "Thread should have self-terminated by now: " + (finishBy - System.currentTimeMillis());
          if (logger.isDebugEnabled())
            logger.warn(message + "\n" + stackTraceToString(thread.getStackTrace()));
          else
            logger.warn(message);
        }

        thread = new Thread() {
          @Override
          public void run() {
            try {
              finishBy = (sender.getPreviousTimestamp() + samplePeriodSeconds) * 1000;
              retryPolicy.run(Metrics.this, finishBy - System.currentTimeMillis());
            }
            catch (final RetryFailureException e) {
              if (logger.isDebugEnabled())
                logger.warn(e.getMessage(), e);
              else
                logger.warn(e.getClass().getName() + ": " + e.getMessage());
            }
          }
        };

        thread.setDaemon(true);
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
    if (timeout <= 0)
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
  public void close() throws Exception {
    closed = true;
    interrupt();
    sender.close();
    if (isAlive()) {
      synchronized (this) {
        wait();
      }
    }
  }
}