package com.lightstep.tracer.retry;

/**
 * Thrown to indicate the ultimate failure of retried invocation(s) by a
 * {@link RetryPolicy}.
 *
 * @see RetryPolicy#run(Retryable)
 */
public class RetryFailureException extends Exception {
  private static final long serialVersionUID = 4067260656337660435L;

  private final int attemptNo;
  private final long delayMs;

  /**
   * Constructs a {@link RetryFailureException} with the specified exception
   * that was the cause of this exception.
   *
   * @param cause The exception that was the cause of this exception.
   * @param attemptNo The attempt number on which the exception was thrown.
   * @param delayMs The delay (in milliseconds) from the previous invocation
   *          attempt.
   */
  public RetryFailureException(final Throwable cause, final int attemptNo, final long delayMs) {
    super("attemptNo = " + attemptNo + ", delayMs = " + delayMs, cause);
    this.attemptNo = attemptNo;
    this.delayMs = delayMs;
  }

  /**
   * Constructs a {@link RetryFailureException} without a specified exception
   * cause.
   *
   * @param attemptNo The attempt number on which the exception was thrown.
   * @param delayMs The delay (in milliseconds) from the previous invocation
   *          attempt.
   */
  public RetryFailureException(final int attemptNo, final long delayMs) {
    this(null, attemptNo, delayMs);
  }

  /**
   * Returns the attempt number on which the exception was thrown.
   *
   * @return The attempt number on which the exception was thrown.
   */
  public int getAttemptNo() {
    return this.attemptNo;
  }

  /**
   * Returns the delay (in milliseconds) from the previous invocation attempt.
   *
   * @return The delay (in milliseconds) from the previous invocation attempt.
   */
  public long getDelayMs() {
    return this.delayMs;
  }
}