package com.aniamadej.sublokator.testService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class RequestSenderService {

  @Autowired
  private TestRestTemplate testRestTemplate;

  public ResponseEntity<String> sendGet(String destinationUrl) {
    return testRestTemplate.getForEntity(destinationUrl, String.class);
  }

  public ResponseEntity<String> sendPost(String url, String refererUrl,
                                         MultiValueMap<String, String> formInputs) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("referer", refererUrl);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<MultiValueMap<String, String>> httpEntity =
        new HttpEntity<>(formInputs, headers);

    return testRestTemplate.postForEntity(url, httpEntity, String.class);
  }

  public ResponseEntity<String> sendPost(String url, String refererUrl) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("referer", refererUrl);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<String> httpEntity =
        new HttpEntity<>( headers);

    return testRestTemplate.postForEntity(url, httpEntity, String.class);
  }

  public ResponseEntity<String> sendPost(String url) {
    return testRestTemplate.postForEntity(url, null, String.class);
  }


}
