package com.aniamadej.sublokator.util;

public final class Mappings {

  public static final String CONNECTIONS_PAGE = "/connections";
  public static final String CONNECTION_PAGE = "/connection";
  public static final String METER_PAGE = "/meter";
  public static final String READING_PAGE = "/reading";
  public static final String METERS_SUBPAGE = "/meters";
  public static final String ADD = "/add";
  public static final String METERS_ADD_SUBPAGE =
      Mappings.METERS_SUBPAGE + Mappings.ADD;
  public static final String CONNECTION_ADD =
      Mappings.CONNECTIONS_PAGE + Mappings.ADD;
  public static final String DEACTIVATE = "/deactivate";
  public static final String RESET = "/reset";
  public static final String REACTIVATE = "/reactivate";
  public static final String DELETE = "/delete";
  private static final String READINGS_SUBPAGE = "/readings";
  public static final String READING_ADD_SUBPAGE =
      Mappings.READINGS_SUBPAGE + Mappings.ADD;


  private Mappings() {
  }
}
