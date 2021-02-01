package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.testService.RequestSenderService;
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
class MediaControllerE2ETests {

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
  private ReadingRepository readingRepository;

  @Autowired
  private RequestSenderService requestSenderService;

  private String urlPrefix;


  private MediumConnection medium1;
  private MediumConnection medium2;

  private MediumMeter mediumMeter1;
  private MediumMeter mediumMeter2;
  private MediumMeter mediumMeter3;
  private MediumMeter mediumMeter4;

  @BeforeAll
  public void init() {

    urlPrefix = "http://localhost:" + port;

    medium1 = new MediumConnection();
    medium1.setMediumName("Gaz");

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

    medium2 = new MediumConnection();
    medium2.setMediumName("Prąd");
    medium2 = mediumConnectionRepository.save(medium2);


  }

  @Test
  @DisplayName("result of get request to media page should "
      + "contain names of saved media, links to their pages, title of page, "
      + "and link to media connection adding page")
  public void httpGet_returnsMediaPage() {

    int numberOfMedia = mediumConnectionService.getNamesList().size();

    String destinationUrl = urlPrefix + Mappings.MEDIA_PAGE;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

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
        .anyMatch(a -> a.text().equals(medium1.getMediumName())
            && a.attr("href")
            .equals(Mappings.MEDIUM_PAGE + "/" + medium1.getId())));

    assertTrue(media.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(medium2.getMediumName())
            && a.attr("href")
            .equals(Mappings.MEDIUM_PAGE + "/" + medium2.getId())));


    // media adding link
    String linkText = getMessage("page.addMedium");

    assertTrue(webPage.select("a").stream().anyMatch(a ->
        a.text().equals(linkText) && a.attr("href")
            .equals(Mappings.MEDIA_ADD)));
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

    String destinationUrl = urlPrefix + Mappings.MEDIA_ADD;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

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
      "post request to medium connection adding page should add medium meter "
          + "with provided name and redirect to valid page with added medium "
          + "on the list")
  public void httpPost_addsMediumConnectionWithGivenName() {

    int initialNumberOfMedia = mediumConnectionService.getNamesList().size();

    String addedMediumName = "bbb";

    String destinationUrl =
        urlPrefix + Mappings.MEDIA_ADD + "?"
            + Attributes.MEDIUM_NAME + "=" + addedMediumName;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl);

    Long addedMediumId = mediumConnectionService.getNamesList().stream()
        .map(NumberedName::getId).max(Long::compareTo).get();

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    String responseBody = responseEntity.getBody();
    
    Document webPage = Jsoup.parse(responseBody);

    Element metersButton = webPage.getElementById("metersButton");
    String addMeterLinkText = getMessage("page.meters");
    String addMeterLinkHref = Mappings.MEDIUM_PAGE + "/" + addedMediumId
        + Mappings.METERS_SUBPAGE;

    assertEquals(metersButton.attr("href"), addMeterLinkHref);
    assertEquals(metersButton.text(), addMeterLinkText);

  }

}