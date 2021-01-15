package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.service.ReadingService;
import com.aniamadej.sublokator.util.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ReadingsController {

  private final ReadingService readingService;


  @Autowired
  ReadingsController(
      ReadingService readingService) {
    this.readingService = readingService;
  }

  @PostMapping(Mappings.READING_PAGE + "/{readingId}" + Mappings.DELETE)
  public String showMediumMeter(@PathVariable("readingId") long readingId) {
    Long meterId;
    meterId = readingService.findMediumId(readingId);
    readingService.delete(readingId);
    String url = Mappings.METER_PAGE + "/" + meterId;
    return "redirect:" + url;
  }
}
