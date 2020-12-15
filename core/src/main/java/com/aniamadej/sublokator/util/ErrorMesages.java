package com.aniamadej.sublokator.util;

public final class ErrorMesages {
  public static final String NO_MEDIUM_CONNECTION_ID =
      "error.connectionNotExists";
  public static final String WRONG_READING_VALUE = "error.wrongReadingValue";
  public static final String NO_METER_ID = "error.meterNotExists";
  public static final String DEACTIVATION_BEFORE_ACTIVATION
      = "error.deactivationBeforeActivation";
  public static final String FUTURE_DEACTIVATION = "error.futureDeactivation";
  public static final String READING_BEFORE_ACTIVATION
      = "error.readingBeforeActivation";
  public static final String READING_AFTER_DEACTIVATION
      = "error.readingAfterDeactivation";
  public static final String DEACTIVATION_BEFORE_LAST_READING =
      "error.deactivationBeforeLastReading";
  public static final String NOT_RESETTABLE = "error.notResettable";
  public static final String RESET_BEFORE_READING = "error.resetBeforeReading";
  public static final String READING_AT_RESET = "error.readingAtReset";

  private ErrorMesages() {
  }
}
