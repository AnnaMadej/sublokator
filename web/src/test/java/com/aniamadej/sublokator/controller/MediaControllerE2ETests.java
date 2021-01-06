package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;


import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Mappings;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MediaControllerE2ETests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private MediumConnectionService mediumConnectionService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private MediumMeterService mediumMeterService;

  private String mediumName1 = "Gaz";
  private String mediumName2 = "Prąd";
  private String meter1Number = "Licznik1(Active, Ressetable)";
  private String meter1Unit = "kwh";

  private String meter2Number = "Licznik2(Active, Not ressetable)";
  private String meter2Unit = "m3";

  private String meter3Number = "Licznik2(Inactive, Ressetable)";
  private String meter3Unit = "unit";

  private String meter4Number = "Licznik2(Inactive, Not resettable)";
  private String meter4Unit = "unit";

  @BeforeAll
  public void init() throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException {
    mediumConnectionService.save(mediumName1);
    mediumConnectionService.save(mediumName2);

    MediumMeterForm mediumMeterForm = new MediumMeterForm();
    mediumMeterForm.setActiveSince(LocalDate.now().toString());
    mediumMeterForm.setFirstReading(12.);
    mediumMeterForm.setNumber(meter1Number);
    mediumMeterForm.setUnitName(meter1Unit);
    mediumMeterForm.setResettable(true);
    mediumConnectionService.addMediumMeter(1L, mediumMeterForm);

    mediumMeterForm.setActiveSince(LocalDate.now().minusDays(1).toString());
    mediumMeterForm.setFirstReading(11.);
    mediumMeterForm.setNumber(meter2Number);
    mediumMeterForm.setUnitName(meter2Unit);
    mediumMeterForm.setResettable(false);
    mediumConnectionService.addMediumMeter(1L, mediumMeterForm);

    mediumMeterForm.setActiveSince(LocalDate.now().minusDays(1).toString());
    mediumMeterForm.setFirstReading(11.);
    mediumMeterForm.setNumber(meter3Number);
    mediumMeterForm.setUnitName(meter3Unit);
    mediumMeterForm.setResettable(true);
    mediumConnectionService.addMediumMeter(1L, mediumMeterForm);

    mediumMeterService.deactivate(3L, LocalDate.now().toString());

    mediumMeterForm.setActiveSince(LocalDate.now().minusDays(1).toString());
    mediumMeterForm.setFirstReading(11.);
    mediumMeterForm.setNumber(meter4Number);
    mediumMeterForm.setUnitName(meter4Unit);
    mediumMeterForm.setResettable(false);
    mediumConnectionService.addMediumMeter(1L, mediumMeterForm);

    mediumMeterService.deactivate(3L, LocalDate.now().toString());
    mediumMeterService.deactivate(4L, LocalDate.now().toString());

  }

  @Test
  @DisplayName("result of get request to media page should "
      + "contain names of saved media, links to their pages, title of page, "
      + "and link to connection adding page")
  public void httpGet_returnsMediaPage() throws Exception {

    int numberOfMedia = mediumConnectionService.getNamesList().size();

    log.info("NUMBER: {}", numberOfMedia);
    String result =
        testRestTemplate.getForObject(Mappings.MEDIA_PAGE, String.class);

    // page title
    assertThat(result)
        .contains(messageSource
            .getMessage("page.mediaConnections", null,
                LocaleContextHolder.getLocale()));

    // number of media
    int numberOfListEntries = result.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(2);


    // media names
    assertThat(result)
        .contains(mediumName1);

    assertThat(result)
        .contains(mediumName2);

    // media links
    assertThat(result)
        .contains("href=\"" + Mappings.MEDIUM_PAGE + "/1\"");

    assertThat(result)
        .contains("href=\"" + Mappings.MEDIUM_PAGE + "/2\"");

    // media adding button text
    assertThat(result)
        .contains(messageSource
            .getMessage("page.addMedium", null,
                LocaleContextHolder.getLocale()));

    // media adding button link
    assertThat(result)
        .contains("href=\"" + Mappings.MEDIA_ADD + "\"");

  }


  @Test
  @DisplayName("result of get request to existing medium page should "
      + "contain title of page, name of medium and link to medium meters")
  public void httpGet_returnsMediumPage() throws Exception {
    long mediumId = 1L;
    String result =
        testRestTemplate
            .getForObject(Mappings.MEDIUM_PAGE + "/" + mediumId, String.class);

    String mediumName = mediumConnectionService.getMediumName(mediumId);

    // page title
    assertThat(result)
        .contains(messageSource
            .getMessage("page.connectedMedium", null,
                LocaleContextHolder.getLocale()));

    // meters link text
    assertThat(result)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // meters link href
    assertThat(result)
        .contains(
            "href=\"" + Mappings.MEDIUM_PAGE + "/1" + Mappings.METERS_SUBPAGE
                + "\"");

    // medium name
    assertThat(result)
        .contains(mediumName);
  }

  @Test
  @DisplayName("result of get request to medium meters subpage without any "
      + "parameter should contain page title, medium name, "
      + "and list of active medium meters and show inactive button")
  public void httpGet_returnsDefaultMediumMetersList() {
    long mediumId = 1L;
    String result =
        testRestTemplate
            .getForObject(
                Mappings.MEDIUM_PAGE + "/" + mediumId
                    + Mappings.METERS_SUBPAGE,
                String.class);
    log.info("result: {}", result);

    // page title
    assertThat(result)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // medium name
    String mediumName = mediumConnectionService.getMediumName(mediumId);
    assertThat(result)
        .contains(mediumName);
    // number of list entries
    int numberOfListEntries = result.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(2);

    // active meters names
    assertThat(result)
        .contains(meter1Number);

    assertThat(result)
        .contains(meter2Number);

    // meters links
    assertThat(result)
        .contains("href=\"" + Mappings.METER_PAGE + "/1\"");

    assertThat(result)
        .contains("href=\"" + Mappings.METER_PAGE + "/2\"");

    // show inactive button label
    assertThat(result)
        .contains(messageSource
            .getMessage("page.showInactive", null,
                LocaleContextHolder.getLocale()));

    // show inactive button link
    assertThat(result)
        .contains("href=\"?inactive=true\"");
  }

  @Test
  @DisplayName(
      "result of get request to medium meters subpage with inactive=false "
          + "parameter should contain page title, medium name, "
          + "and list of active medium meters and show inactive button")
  public void httpGet_returnsActiveMediumMetersList() {
    long mediumId = 1L;
    String result =
        testRestTemplate
            .getForObject(
                Mappings.MEDIUM_PAGE + "/" + mediumId
                    + Mappings.METERS_SUBPAGE + "?inactive=false",
                String.class);
    log.info("result: {}", result);

    // page title
    assertThat(result)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // medium name
    String mediumName = mediumConnectionService.getMediumName(mediumId);
    assertThat(result)
        .contains(mediumName);
    // number of list entries
    int numberOfListEntries = result.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(2);

    // active meters names
    assertThat(result)
        .contains(meter1Number);

    assertThat(result)
        .contains(meter2Number);

    // meters links
    assertThat(result)
        .contains("href=\"" + Mappings.METER_PAGE + "/1\"");

    assertThat(result)
        .contains("href=\"" + Mappings.METER_PAGE + "/2\"");

    // show inactive button label
    assertThat(result)
        .contains(messageSource
            .getMessage("page.showInactive", null,
                LocaleContextHolder.getLocale()));

    // show inactive button link
    assertThat(result)
        .contains("href=\"?inactive=true\"");
  }

  @Test
  @DisplayName(
      "result of get request to medium meters subpage with inactive=true "
          + "parameter should contain page title, medium name, "
          + "and list of inactive medium meters and show active button")
  public void httpGet_returnsInactiveMediumMetersList() {
    long mediumId = 1L;
    String result =
        testRestTemplate
            .getForObject(
                Mappings.MEDIUM_PAGE + "/" + mediumId
                    + Mappings.METERS_SUBPAGE + "?inactive=true",
                String.class);
    log.info("result: {}", result);

    // page title
    assertThat(result)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // medium name
    String mediumName = mediumConnectionService.getMediumName(mediumId);
    assertThat(result)
        .contains(mediumName);
    // number of list entries
    int numberOfListEntries = result.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(2);

    // active meters names
    assertThat(result)
        .contains(meter3Number);

    assertThat(result)
        .contains(meter4Number);

    // meters links
    assertThat(result)
        .contains("href=\"" + Mappings.METER_PAGE + "/3\"");

    assertThat(result)
        .contains("href=\"" + Mappings.METER_PAGE + "/4\"");

    // show active button label
    assertThat(result)
        .contains(messageSource
            .getMessage("page.showActive", null,
                LocaleContextHolder.getLocale()));

    // show active button link
    assertThat(result)
        .contains("href=\"?inactive=false\"");
  }
}