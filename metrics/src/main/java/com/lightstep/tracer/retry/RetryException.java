package com.lightstep.tracer.retry;

/**
 * Class representing a retryable exception that qualifies for retried
 * invocation, which is the default behavior defined in
 * {@link RetryPolicy#retryOn(Exception)}.
 */
public class RetryException extends RuntimeException {
  private static final long serialVersionUID = -215964084300420516L;

  /**
   * Creates a {@link RetryException} with no detail message.
   */
  public RetryException() {
  }

  /**
   * Creates a {@link RetryException} with the specified detail message.
   *
   * @param message The detail message.
   */
  public RetryException(final String message) {
    super(message);
  }

  /**
   * Constructs a {@link RetryException} with the specified exception that was
   * the cause of this exception.
   *
   * @param cause The exception that was the cause of this exception.
   */
  public RetryException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@link RetryException} with the specified detail message and
   * exception that was the cause of this exception.
   *
   * @param message The detail message.
   * @param cause The exception that was the cause of this exception.
   */
  public RetryException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@link RetryException} with the specified detail message,
   * cause, suppression enabled or disabled, and writable stack trace enabled or
   * disabled.
   *
   * @param message The detail message.
   * @param cause The exception that was the cause of this exception.
   * @param enableSuppression Whether or not suppression is enabled or disabled.
   * @param writableStackTrace Whether or not the stack trace should be
   *          writable.
   */
  public RetryException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}