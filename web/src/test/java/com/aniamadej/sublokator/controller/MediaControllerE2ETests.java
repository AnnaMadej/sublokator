package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import java.time.LocalDate;
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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

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
  private
  MediumConnectionRepository mediumConnectionRepository;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  MediumMeterRepository mediumMeterRepository;

  private MediumConnection medium1;
  private MediumConnection medium2;

  private MediumMeter mediumMeter1;
  private MediumMeter mediumMeter2;
  private MediumMeter mediumMeter3;
  private MediumMeter mediumMeter4;

  @BeforeAll
  public void init() {

    medium1 = new MediumConnection();
    medium1.setMediumName("Gaz");
    medium2 = new MediumConnection();
    medium2.setMediumName("Prąd");
    medium1 = mediumConnectionRepository.save(medium1);
    medium2 = mediumConnectionRepository.save(medium2);

    mediumMeter1 = new MediumMeter();
    mediumMeter1.setNumber("Active, Resettable");
    mediumMeter1.setActiveSince(LocalDate.now());
    mediumMeter1.setResettable(true);


    mediumMeter2 = new MediumMeter();
    mediumMeter2.setNumber("Active, Not resettable");
    mediumMeter2.setActiveSince(LocalDate.now());
    mediumMeter2.setResettable(false);

    mediumMeter3 = new MediumMeter();
    mediumMeter3.setNumber("Inactive, Resettable");
    mediumMeter3.setActiveSince(LocalDate.now().minusDays(1));
    mediumMeter3.setActiveUntil(LocalDate.now());
    mediumMeter3.setResettable(true);

    mediumMeter4 = new MediumMeter();
    mediumMeter4.setNumber("Inactive, Not resettable");
    mediumMeter4.setActiveSince(LocalDate.now().minusDays(1));
    mediumMeter4.setActiveUntil(LocalDate.now());
    mediumMeter4.setResettable(false);

    mediumMeter1.setMediumConnection(medium1);
    mediumMeter2.setMediumConnection(medium1);
    mediumMeter3.setMediumConnection(medium1);
    mediumMeter4.setMediumConnection(medium1);

    mediumMeter1 = mediumMeterRepository.save(mediumMeter1);
    mediumMeter2 = mediumMeterRepository.save(mediumMeter2);
    mediumMeter3 = mediumMeterRepository.save(mediumMeter3);
    mediumMeter4 = mediumMeterRepository.save(mediumMeter4);

  }

  @Test
  @DisplayName("result of get request to media page should "
      + "contain names of saved media, links to their pages, title of page, "
      + "and link to connection adding page")
  public void httpGet_returnsMediaPage() {

    int numberOfMedia = mediumConnectionService.getNamesList().size();

    ResponseEntity<String> responseEntity =
        testRestTemplate
            .getForEntity("http://localhost:" + port + Mappings.MEDIA_PAGE,
                String.class);
    String responseBody = responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title header
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.mediaConnections", null,
                LocaleContextHolder.getLocale()));

    // number of media
    int numberOfListEntries = responseBody.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(numberOfMedia);


    // media names
    assertThat(responseBody)
        .contains(medium1.getMediumName());

    assertThat(responseBody)
        .contains(medium2.getMediumName());

    // media links
    assertThat(responseBody)
        .contains("href=\"" + Mappings.MEDIUM_PAGE + "/" + medium1.getId());

    assertThat(responseBody)
        .contains("href=\"" + Mappings.MEDIUM_PAGE + "/" + medium2.getId());

    // media adding button text
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.addMedium", null,
                LocaleContextHolder.getLocale()));

    // media adding button link
    assertThat(responseBody)
        .contains("href=\"" + Mappings.MEDIA_ADD + "\"");

  }


  @Test
  @DisplayName("result of get request to existing medium page should "
      + "contain title of page, name of medium and links to medium meters")
  public void httpGet_returnsMediumPage() throws Exception {
    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/" + medium1
                .getId(),
            String.class);

    String responseBody = responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.connectedMedium", null,
                LocaleContextHolder.getLocale()));

    // meters link text
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // meters link href
    assertThat(responseBody)
        .contains(
            "href=\"" + Mappings.MEDIUM_PAGE + "/" + medium1.getId()
                + Mappings.METERS_SUBPAGE
                + "\"");

    // medium name
    assertThat(responseBody)
        .contains(medium1.getMediumName());
  }

  @Test
  @DisplayName("result of get request to medium meters subpage without any "
      + "parameter should contain page title, medium name, "
      + "and list of active medium meters, showInactive button "
      + "and addMeter button")
  public void httpGet_returnsDefaultMediumMetersList() {
    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
                + medium1.getId() + Mappings.METERS_SUBPAGE,
            String.class);

    String responseBody = responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // medium name
    assertThat(responseBody)
        .contains(medium1.getMediumName());
    // number of list entries
    int numberOfListEntries = responseBody.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(2);

    // active meters names
    assertThat(responseBody)
        .contains(mediumMeter1.getNumber());

    assertThat(responseBody)
        .contains(mediumMeter2.getNumber());

    // meters links
    assertThat(responseBody)
        .contains("href=\"" + Mappings.METER_PAGE + "/" + mediumMeter1.getId()
            + "\"");

    assertThat(responseBody)
        .contains("href=\"" + Mappings.METER_PAGE + "/" + mediumMeter2.getId()
            + "\"");

    // show inactive button label
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.showInactive", null,
                LocaleContextHolder.getLocale()));

    // show inactive button link
    assertThat(responseBody)
        .contains("href=\"?inactive=true\"");

    // meter add button label
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.addMeter", null,
                LocaleContextHolder.getLocale()));

    // meter add button link
    assertThat(responseBody)
        .contains("href=\"" +
            Mappings.MEDIUM_PAGE + "/" + medium1.getId()
            + Mappings.METERS_ADD_SUBPAGE
            + "\"");
  }

  @Test
  @DisplayName(
      "result of get request to medium meters subpage with inactive=false "
          + "parameter should contain page title, medium name, "
          + "and list of active medium meters, showInactive button "
          + "and addMeter button")
  public void httpGet_returnsActiveMediumMetersList() {

    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
                + medium1.getId() + Mappings.METERS_SUBPAGE + "?inactive=false",
            String.class);

    String responseBody = responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // medium name
    assertThat(responseBody)
        .contains(medium1.getMediumName());
    // number of list entries
    int numberOfListEntries = responseBody.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(2);

    // active meters names
    assertThat(responseBody)
        .contains(mediumMeter1.getNumber());

    assertThat(responseBody)
        .contains(mediumMeter2.getNumber());

    // meters links
    assertThat(responseBody)
        .contains("href=\"" + Mappings.METER_PAGE + "/" + mediumMeter1.getId());

    assertThat(responseBody)
        .contains("href=\"" + Mappings.METER_PAGE + "/" + mediumMeter2.getId());

    // show inactive button label
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.showInactive", null,
                LocaleContextHolder.getLocale()));

    // show inactive button link
    assertThat(responseBody)
        .contains("href=\"?inactive=true\"");

    // meter add button label
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.addMeter", null,
                LocaleContextHolder.getLocale()));

    // meter add button link
    assertThat(responseBody)
        .contains("href=\"" +
            Mappings.MEDIUM_PAGE + "/" + medium1.getId() + Mappings.METERS_ADD_SUBPAGE
            + "\"");
  }

  @Test
  @DisplayName(
      "result of get request to medium meters subpage with inactive=true "
          + "parameter should contain page title, medium name, "
          + "and list of inactive medium meters , showActive button "
          + "and addMeter button")
  public void httpGet_returnsInactiveMediumMetersList() {

    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/" + medium1.getId()
                + Mappings.METERS_SUBPAGE + "?inactive=true",
            String.class);

    String responseBody = responseEntity.getBody();
    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.meters", null,
                LocaleContextHolder.getLocale()));

    // medium name
    assertThat(responseBody)
        .contains(medium1.getMediumName());
    // number of list entries
    int numberOfListEntries = responseBody.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(2);

    // active meters names
    assertThat(responseBody)
        .contains(mediumMeter3.getNumber());

    assertThat(responseBody)
        .contains(mediumMeter4.getNumber());

    // meters links
    assertThat(responseBody)
        .contains("href=\"" + Mappings.METER_PAGE + "/" + mediumMeter3.getId());

    assertThat(responseBody)
        .contains("href=\"" + Mappings.METER_PAGE + "/" + mediumMeter4.getId());

    // show active button label
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.showActive", null,
                LocaleContextHolder.getLocale()));

    // show active button link
    assertThat(responseBody)
        .contains("href=\"?inactive=false\"");

    // meter add button label
    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.addMeter", null,
                LocaleContextHolder.getLocale()));

    // meter add button link
    assertThat(responseBody)
        .contains("href=\"" +
            Mappings.MEDIUM_PAGE + "/" + medium1.getId() + Mappings.METERS_ADD_SUBPAGE
            + "\"");
  }

  @Test
  @DisplayName(
      "result of get request to medium connection adding page should contain"
          + "page title, medium name label, medium name input and add button")
  public void httpGet_returnsMediumConnectionAddingPage() {

    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity("http://localhost:" + port + Mappings.MEDIA_ADD,
            String.class);
    String responseBody = responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());

    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.addMedium", null,
                LocaleContextHolder.getLocale()));

    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.mediumName", null,
                LocaleContextHolder.getLocale()));

    assertThat(responseBody)
        .contains(
            "<input id=\"mediumNameInput\" type=\"text\" name=\""
                + Attributes.MEDIUM_NAME + "\"/>");

    assertThat(responseBody)
        .contains(messageSource
            .getMessage("page.addButton", null,
                LocaleContextHolder.getLocale()));
  }

  @Test
  @Transactional
  @DisplayName(
      "post request to medium connection adding page should add medium meter "
          + "with provided name and redirect to valid page with added medium "
          + "on the list")
  public void httpPost_addsMediumConnectionWithGivenName() {

    int initialNumberOfMedia = mediumConnectionService.getNamesList().size();

    String addedMediumName = "nowe medium";

    // no new medium in database yet
    assertTrue(mediumConnectionService.getNamesList().stream()
        .noneMatch(name -> name.getName().equals(addedMediumName)));

    String url =
        "http://localhost:" + port + Mappings.MEDIA_ADD + "?"
            + Attributes.MEDIUM_NAME + "=" + addedMediumName;

    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, null,
            String.class);

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    // medium added to database
    assertTrue(mediumConnectionService.getNamesList().stream()
        .anyMatch(name -> name.getName().equals(addedMediumName)
            && name.getId() == initialNumberOfMedia + 1));

    String responseBody = responseEntity.getBody();

    // added medium name on page
    assertThat(responseBody)
        .contains(addedMediumName);

    // added medium link on page
    assertThat(responseBody)
        .contains("href=\"" + Mappings.MEDIUM_PAGE + "/" + (initialNumberOfMedia
            + 1));

    // increased number of media on page
    int numberOfListEntries = responseBody.split("<li>").length - 1;
    assertThat(numberOfListEntries)
        .isEqualTo(initialNumberOfMedia + 1);

  }

}