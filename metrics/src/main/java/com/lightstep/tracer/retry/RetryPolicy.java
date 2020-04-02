package com.lightstep.tracer.retry;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A policy that defines the conditions and timing of when retries should be
 * performed.
 * <p>
 * The {@link #retryOn(Exception)} method specifies the exception conditions of
 * when a retry should occur.
 * <p>
 * The {@link #run(Retryable)} method is the entrypoint for a {@link Retryable}
 * object to be executed.
 */
public abstract class RetryPolicy implements Serializable {
  private static final long serialVersionUID = -8480057566592276543L;
  private static final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);

  private final int maxRetries;
  private final double jitter;

  /**
   * Creates a new {@link RetryPolicy} with the specified {@code maxRetries}
   * value.
   *
   * @param maxRetries A positive value representing the number of retry
   *          attempts allowed by the {@link RetryPolicy}.
   * @param jitter The factor multiplier to be applied to
   *          {@link #getDelayMs(int)} to thereafter be added to the delay for
   *          each retry.
   * @throws IllegalArgumentException If {@code maxRetries} or {@code jitter} is
   *           negative.
   */
  public RetryPolicy(final int maxRetries, final double jitter) {
    if (maxRetries < 0)
      throw new IllegalArgumentException("maxRetries (" + maxRetries + ") is negative");

    if (jitter < 0)
      throw new IllegalArgumentException("jitter (" + jitter + ") is negative");

    this.maxRetries = maxRetries;
    this.jitter = jitter;
    if (maxRetries <= 0)
      throw new IllegalArgumentException("maxRetries (" + maxRetries + ") must be a positive value");
  }

  /**
   * Specifies the exception conditions of when a retry should occur.
   *
   * @param e The exception that occurred during execution of a
   *          {@link Retryable} object.
   * @return {@code true} if a retry should occur, otherwise {@code false}.
   */
  protected boolean retryOn(final Exception e) {
    return e instanceof RetryException;
  }

  /**
   * The entrypoint for a {@link Retryable} object to be executed. Exceptions in
   * {@link Retryable#retry(RetryPolicy,int)} will be considered for retry if
   * the number of {@link #maxRetries} has not been exceeded, and
   * {@link #retryOn(Exception)} returns {@code true}.
   *
   * @param <T> The type of the result object.
   * @param retryable The {@link Retryable} object to run.
   * @return The resulting value from {@link Retryable#retry(RetryPolicy,int)}.
   * @throws RetryFailureException If retry attempts have exceeded
   *           {@link #maxRetries}, or if {@link #retryOn(Exception)} returns
   *           {@code false}.
   * @throws NullPointerException If {@code retryable} is null.
   */
  public final <T>T run(final Retryable<T> retryable) throws RetryFailureException {
    return run0(retryable, 0);
  }

  /**
   * The entrypoint for a {@link Retryable} object to be executed. Exceptions in
   * {@link Retryable#retry(RetryPolicy,int)} will be considered for retry if
   * the number of {@link #maxRetries} has not been exceeded, and
   * {@link #retryOn(Exception)} returns {@code true}.
   *
   * @param <T> The type of the result object.
   * @param retryable The {@link Retryable} object to run.
   * @param timeout The maximum time to retry in milliseconds.
   * @return The resulting value from {@link Retryable#retry(RetryPolicy,int)}.
   * @throws RetryFailureException If retry attempts have exceeded
   *           {@link #maxRetries}, if {@link #retryOn(Exception)} returns
   *           {@code false}, or if {@code timeout} is exceeded.
   * @throws IllegalArgumentException If {@code timeout} is negative.
   * @throws NullPointerException If the value of {@code timeout} is negative.
   */
  public final <T>T run(final Retryable<T> retryable, final long timeout) throws RetryFailureException {
    if (timeout < 0)
      throw new IllegalArgumentException("timeout value (" + timeout + ") is negative");

    return run0(retryable, timeout);
  }

  private final <T>T run0(final Retryable<T> retryable, final long timeout) throws RetryFailureException {
    final long startTime = System.currentTimeMillis();
    long runTime = 0;
    for (int attemptNo = 1;; ++attemptNo) {
      try {
        return retryable.retry(this, attemptNo);
      }
      catch (final RetryFailureException e) {
        throw e;
      }
      catch (final Exception e) {
        if (attemptNo > maxRetries || !retryOn(e))
          throw new RetryFailureException(e, attemptNo, getDelayMs(attemptNo - 1));

        long delayMs = getDelayMs(attemptNo);
        if (jitter > 0)
          delayMs *= jitter * Math.random() + 1;

        if (timeout > 0) {
          final long remaining = timeout - runTime;
          if (remaining <= 0)
            throw new RetryFailureException(attemptNo, delayMs);

          if (remaining < delayMs)
            delayMs = remaining;
        }

        try {
          Thread.sleep(delayMs);
          runTime = System.currentTimeMillis() - startTime;
        }
        catch (final InterruptedException ie) {
          throw new RetryFailureException(ie, attemptNo, delayMs);
        }
      }

      if (logger.isDebugEnabled())
        logger.debug("Retrying attemptNo = " + attemptNo + ", runTime = " + runTime);
    }
  }

  /**
   * Returns the number of retry attempts allowed by this {@link RetryPolicy}.
   *
   * @return The number of retry attempts allowed by this {@link RetryPolicy}.
   */
  public int getMaxRetries() {
    return this.maxRetries;
  }

  /**
   * Returns the delay in milliseconds for the specified attempt number. This
   * method is intended to be implemented by a subclass to define the backoff
   * function for retry attempts.
   *
   * @param attemptNo The attempt number, starting with {@code 1}.
   * @return The delay in milliseconds for the specified attempt number.
   */
  public abstract long getDelayMs(int attemptNo);
}