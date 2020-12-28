package com.aniamadej.sublokator.util;

public final class ErrorMessages {
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
  public static final String RESET_NOT_AFTER_LAST_READING =
      "error.resetNotAfterLastReading";
  public static final String READING_AT_RESET = "error.readingAtReset";
  public static final String BLANK_NAME = "error.blankName";
  public static final String TOO_LONG_NAME = "error.tooLongName";
  public static final String BLANK_DATE = "error.blankDate";
  public static final String DUPLICATE_READING = "error.duplicateReading";
  public static final String NO_READING_ID = "error.noReadingId";
  public static final String FIRST_DELETE = "error.firstDelete";
  public static final String ZERO_DELETE = "error.zeroDelete";
  public static final String NEGATIVE_READING = "error.negativeReading";

  private ErrorMessages() {
  }
}
