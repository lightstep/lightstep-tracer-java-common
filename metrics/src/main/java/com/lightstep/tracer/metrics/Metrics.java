package com.lightstep.tracer.metrics;

import java.util.Timer;
import java.util.TimerTask;

import com.lightstep.tracer.retry.ExponentialBackoffRetryPolicy;
import com.lightstep.tracer.retry.RetryFailureException;
import com.lightstep.tracer.retry.RetryPolicy;
import com.lightstep.tracer.retry.Retryable;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class Metrics extends TimerTask implements Retryable<Void>, AutoCloseable {
  private final MetricGroup[] metricGroups = {new CpuMetricGroup(), new NetworkMetricGroup(), new MemoryMetricGroup(), new GcMetricGroup()};

  private final SystemInfo systemInfo = new SystemInfo();
  private final HardwareAbstractionLayer hal = systemInfo.getHardware();

  private final int samplePeriodSeconds;
  private final ProtobufSender sender;

  private static final int attempts = Integer.MAX_VALUE;
  private static final int startDelay = 1000;
  private static final int factor = 2;
  private static final int maxDelay = Integer.MAX_VALUE;

  private final ExponentialBackoffRetryPolicy retryPolicy = new ExponentialBackoffRetryPolicy(attempts, startDelay, factor, maxDelay, true, 1);

  private final Timer timer = new Timer(true);

  Metrics(final String componentName, final int samplePeriodSeconds) {
    if (samplePeriodSeconds < 1)
      throw new IllegalArgumentException("samplePeriodSeconds (" + samplePeriodSeconds + ") < 1");

    this.samplePeriodSeconds = samplePeriodSeconds;
    this.sender = new ProtobufSender(componentName);
    timer.schedule(this, System.currentTimeMillis() - (System.currentTimeMillis() / 1000) * 1000, samplePeriodSeconds * 1000);
  }

  @Override
  public void run() {
    new Thread() {
      @Override
      public void run() {
        try {
          retryPolicy.run(Metrics.this);
        }
        catch (final RetryFailureException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }

  @Override
  public Void retry(final RetryPolicy retryPolicy, final int attemptNo) throws Exception {
    final long timeout = (sender.getPreviousTimestamp() + samplePeriodSeconds) * 1000 - System.currentTimeMillis();
    if (timeout < 0)
      throw new RetryFailureException(attemptNo, retryPolicy.getDelayMs(attemptNo - 1));

    sender.run(metricGroups, hal, timeout);
    return null;
  }

  @Override
  public void close() {
    timer.cancel();
  }
}