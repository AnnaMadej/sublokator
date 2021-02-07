package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.model.Medium;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.MediumRepository;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.testService.DataGeneratorService;
import com.aniamadej.sublokator.testService.HttpRequestSenderService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import java.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConnectionsControllerE2ETests {

  @LocalServerPort
  private int port;

  @Autowired
  private MediumConnectionService mediumConnectionService;

  @Autowired
  private
  MediumConnectionRepository mediumConnectionRepository;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private ErrorMessageSource errorsMessageSource;

  @Autowired
  private MediumMeterRepository mediumMeterRepository;

  @Autowired
  private MediumRepository mediumRepository;

  @Autowired
  private HttpRequestSenderService httpRequestSenderService;

  @Autowired
  private DataGeneratorService dataGeneratorService;

  private String urlPrefix;


  private MediumConnection connection1;
  private MediumConnection connection2;

  private MediumMeter mediumMeter1;
  private MediumMeter mediumMeter2;
  private MediumMeter mediumMeter3;
  private MediumMeter mediumMeter4;

  private Medium medium1;
  private Medium medium2;

  @BeforeAll
  public void init() {

    urlPrefix = "http://localhost:" + port;

    medium1 = new Medium(dataGeneratorService.generateUniqueMediumName());

    connection1 = new MediumConnection();
    connection1.setDescription("connection 1");
    connection1.setMedium(medium1);

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

    mediumMeter1.setMediumConnection(connection1);
    mediumMeter2.setMediumConnection(connection1);
    mediumMeter3.setMediumConnection(connection1);
    mediumMeter4.setMediumConnection(connection1);

    mediumMeter1 = mediumMeterRepository.save(mediumMeter1);
    mediumMeter2 = mediumMeterRepository.save(mediumMeter2);
    mediumMeter3 = mediumMeterRepository.save(mediumMeter3);
    mediumMeter4 = mediumMeterRepository.save(mediumMeter4);

    medium2 = new Medium(dataGeneratorService.generateUniqueMediumName());

    connection2 = new MediumConnection();
    connection2.setDescription("connection2");
    connection2.setMedium(medium2);
    connection2 = mediumConnectionRepository.save(connection2);


  }

  @Test
  @DisplayName("result of get request to media page should "
      + "contain names of saved media, links to their pages, title of page, "
      + "and link to media connection adding page")
  public void httpGet_returnsMediaPage() {

    int numberOfMedia = mediumConnectionService.getNamesList().size();

    String destinationUrl = urlPrefix + Mappings.CONNECTIONS_PAGE;

    ResponseEntity<String> responseEntity =
        httpRequestSenderService.sendGet(destinationUrl);

    assertEquals(200, responseEntity.getStatusCodeValue());

    Document webPage = Jsoup.parse(responseEntity.getBody());

    // page title header
    String mediaConnectionsText = getMessage("page.mediaConnections");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(mediaConnectionsText);


    // number of media
    Element mediaList = webPage.getElementById("namesList");

    Elements media = mediaList.select("li");

    assertThat(media.size()).isEqualTo(numberOfMedia);

    // proper media links
    assertTrue(media.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().contains(connection1.getDescription())
            && a.text().contains(medium1.getName())
            && a.attr("href")
            .equals(Mappings.CONNECTION_PAGE + "/" + connection1.getId())));

    assertTrue(media.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().contains(connection2.getDescription())
            && a.text().contains(medium2.getName())
            && a.attr("href")
            .equals(Mappings.CONNECTION_PAGE + "/" + connection2.getId())));

    // media adding link
    String linkText = getMessage("page.addMedium");

    assertTrue(webPage.select("a").stream().anyMatch(a ->
        a.text().equals(linkText) && a.attr("href")
            .equals(Mappings.CONNECTION_ADD)));
  }

  private String getMessage(String messageCode) {
    return messageSource
        .getMessage(messageCode, null,
            LocaleContextHolder.getLocale());
  }

  @Test
  @DisplayName(
      "result of get request to medium connection adding page should contain"
          + "page title, medium name label, medium name input and add button")
  public void httpGet_returnsMediumConnectionAddingPage() {

    String destinationUrl = urlPrefix + Mappings.CONNECTION_ADD;

    ResponseEntity<String> responseEntity =
        httpRequestSenderService.sendGet(destinationUrl);

    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title header
    String addNewMediumText = getMessage("page.addMedium");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(addNewMediumText);

    Element form = webPage.getElementById("newMediumForm");
    Element label = form.select("label").get(0);

    String labelText = getMessage("page.mediumName");

    assertThat(label.text()).isEqualTo(labelText);

    Element input = form.select("input").get(0);
    assertThat(input.attr("name")).isEqualTo(Attributes.MEDIUM_NAME);
    assertThat(input.attr("type")).isEqualTo("text");

    Element button = form.select("button").get(0);
    assertThat(button.attr("type")).isEqualTo("submit");

    String addButtonText = getMessage("page.addButton");

    assertThat(button.text()).isEqualTo(addButtonText);

  }

  @Test
  @DisplayName(
      "post request to connection adding page should add medium connection "
          + "with provided name to existing medium with provided name "
          + "and redirect to valid page of  added connection")
  public void httpPost_addsMediumConnectionWithGivenName() {


    String addedConnectionDescription = "desc";
    String mediumName = medium2.getName();

    String destinationUrl =
        urlPrefix + Mappings.CONNECTION_ADD + "?"
            + Attributes.MEDIUM_NAME + "=" + mediumName + "&"
            + Attributes.DESCRIPTION + "=" + addedConnectionDescription;

    ResponseEntity<String> responseEntity =
        httpRequestSenderService.sendPost(destinationUrl);

    Long addedMediumId = mediumConnectionService.getNamesList().stream()
        .map(NumberedName::getId).max(Long::compareTo).get();

    String responseBody = responseEntity.getBody();

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    Document webPage = Jsoup.parse(responseBody);

    Element mediumNameP = webPage.getElementById("mediumName");

    assertThat(mediumNameP.text()).contains(mediumName);
    assertThat(mediumNameP.text()).contains(addedConnectionDescription);


    Element metersButton = webPage.getElementById("metersButton");
    String metersLinkText = getMessage("page.meters");
    String metersLinkHref = Mappings.CONNECTION_PAGE + "/" + addedMediumId
        + Mappings.METERS_SUBPAGE;

    assertEquals(metersButton.attr("href"), metersLinkHref);
    assertEquals(metersButton.text(), metersLinkText);
  }

  @Test
  @DisplayName(
      "post request to connection adding page should add medium connection "
          + "with provided name to NEW medium with provided name "
          + "and redirect to valid page of  added connection")
  public void httpPost_addsMediumConnectionWithGivenNameToNewMedium() {

    String addedConnectionDescription = "description";
    String mediumName = dataGeneratorService.generateUniqueMediumName();

    String destinationUrl =
        urlPrefix + Mappings.CONNECTION_ADD + "?"
            + Attributes.MEDIUM_NAME + "=" + mediumName + "&"
            + Attributes.DESCRIPTION + "=" + addedConnectionDescription;

    ResponseEntity<String> responseEntity =
        httpRequestSenderService.sendPost(destinationUrl);

    Long addedMediumId = mediumConnectionService.getNamesList().stream()
        .map(NumberedName::getId).max(Long::compareTo).get();

    String responseBody = responseEntity.getBody();

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    Document webPage = Jsoup.parse(responseBody);

    Element mediumNameP = webPage.getElementById("mediumName");

    assertThat(mediumNameP.text()).contains(mediumName);
    assertThat(mediumNameP.text()).contains(addedConnectionDescription);


    Element metersButton = webPage.getElementById("metersButton");
    String metersLinkText = getMessage("page.meters");
    String metersLinkHref = Mappings.CONNECTION_PAGE + "/" + addedMediumId
        + Mappings.METERS_SUBPAGE;

    assertEquals(metersButton.attr("href"), metersLinkHref);
    assertEquals(metersButton.text(), metersLinkText);
  }


}