package com.lightstep.tracer.retry;

/**
 * Interface that provides retry ability for a class when executed via
 * {@link RetryPolicy#run(Retryable)}.
 * <p>
 * This is a functional interface whose functional method is
 * {@link #retry(RetryPolicy,int)}.
 *
 * @param <T> The type of the result of this {@link Retryable}.
 */
@FunctionalInterface
public interface Retryable<T> {
  /**
   * Main run method of the {@link Retryable} that is invoked by a
   * {@link RetryPolicy}, which defines the rules of retry invocations.
   *
   * @param retryPolicy The invoking {@link RetryPolicy}.
   * @param attemptNo The incremental sequence number of the retry attempt.
   * @return The result of the invocation.
   * @throws Exception If an exception occurs.
   */
  T retry(RetryPolicy retryPolicy, int attemptNo) throws Exception;
}