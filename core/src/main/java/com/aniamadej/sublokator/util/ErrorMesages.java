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

  private ErrorMesages() {
  }
}
