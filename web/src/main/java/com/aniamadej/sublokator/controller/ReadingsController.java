package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.service.ReadingService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReadingsController {

  private final ReadingService readingService;
  private final MessageSource errorsMessageSource;

  @Autowired
  ReadingsController(
      ReadingService readingService,
      MessageSource errorsMessageSource) {
    this.readingService = readingService;
    this.errorsMessageSource = errorsMessageSource;
  }

  @PostMapping(Mappings.READING_PAGE + "/{readingId}" + Mappings.DELETE)
  public String showMediumMeter(@PathVariable("readingId") long readingId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
    Long meterId;
    meterId = readingService.findMediumId(readingId);

    try {
      readingService.delete(readingId);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(Attributes.ERROR, e.getMessage());
    }
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }
}
