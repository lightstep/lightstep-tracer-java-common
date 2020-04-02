package com.lightstep.tracer.retry;

/**
 * A {@link RetryPolicy} that defines a maximum number of retries, and a
 * delay for retry attempts that increases exponentially based on a backoff factor.
 */
public class ExponentialBackoffRetryPolicy extends RetryPolicy {
  private static final long serialVersionUID = -6999301056780454011L;

  private final int delayMs;
  private final double backoffFactor;
  private final int maxDelayMs;
  private final boolean noDelayOnFirstRetry;

  /**
   * Creates a new {@link ExponentialBackoffRetryPolicy} with the specified
   * maximum number of retries, and a delay for retry attempts that increases
   * exponentially based on a backoff factor.
   *
   * @param maxRetries A positive value representing the number of retry
   *          attempts allowed by the {@link ExponentialBackoffRetryPolicy}.
   * @param delayMs A positive value representing the delay for the first retry,
   *          in milliseconds, which is also used as the multiplicative factor
   *          for subsequent backed-off delays.
   * @param backoffFactor The base of the backoff exponential function, i.e. a
   *          value of {@code 2} represents a backoff function of {@code 2^a},
   *          where {@code a} is the attempt number.
   * @param maxDelayMs The maximum delay, in milliseconds, which takes effect if
   *          the delay computed by the backoff function is a greater value.
   * @param noDelayOnFirstRetry {@code true} for the first retry to be attempted
   *          immediately, otherwise {@code false} for the first retry to be
   *          attempted after {@code delayMs}.
   * @param jitter The factor multiplier to be applied to {@code delayMs} to
   *          thereafter be added to the delay for each retry.
   * @throws IllegalArgumentException If {@code delayMs}, {@code maxDelayMs},
   *           {@code maxRetries} or {@code jitter} is negative.
   */
  public ExponentialBackoffRetryPolicy(final int maxRetries, final int delayMs, final double backoffFactor, final int maxDelayMs, final boolean noDelayOnFirstRetry, final double jitter) {
    super(maxRetries, jitter);
    this.delayMs = delayMs;
    if (delayMs <= 0)
      throw new IllegalArgumentException("delayMs (" + delayMs + ") must be a positive value");

    this.backoffFactor = backoffFactor;
    if (backoffFactor < 1.0)
      throw new IllegalArgumentException("backoffFactor (" + backoffFactor + ") must be >= 1.0");

    this.maxDelayMs = maxDelayMs;
    if (maxDelayMs <= 0)
      throw new IllegalArgumentException("maxDelayMs (" + maxDelayMs + ") must be a positive value");

    this.noDelayOnFirstRetry = noDelayOnFirstRetry;
  }

  @Override
  public long getDelayMs(final int attemptNo) {
    return Math.min(attemptNo == 1 && noDelayOnFirstRetry ? 0 : (int)(delayMs * StrictMath.pow(backoffFactor, attemptNo - 1)), maxDelayMs);
  }
}