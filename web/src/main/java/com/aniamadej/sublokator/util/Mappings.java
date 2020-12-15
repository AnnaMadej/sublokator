package com.aniamadej.sublokator.util;

public final class Mappings {

  public static final String MEDIA_PAGE = "/media";
  public static final String MEDIUM_PAGE = "/medium";
  public static final String METER_PAGE = "/meter";
  public static final String METERS_SUBPAGE = "/meters";
  public static final String ADD = "/add";
  public static final String METERS_ADD_SUBPAGE =
      Mappings.METERS_SUBPAGE + Mappings.ADD;
  public static final String MEDIA_ADD = Mappings.MEDIA_PAGE + Mappings.ADD;
  public static final String DEACTIVATE = "/deactivate";
  public static final String RESET = "/reset";
  private static final String READINGS_SUBPAGE = "/readings";
  public static final String READING_ADD_SUBPAGE =
      Mappings.READINGS_SUBPAGE + Mappings.ADD;


  private Mappings() {
  }
}
