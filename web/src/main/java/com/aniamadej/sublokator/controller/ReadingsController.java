package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.service.ReadingService;
import com.aniamadej.sublokator.util.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReadingsController {

  private final ReadingService readingService;

  @Autowired
  ReadingsController(
      ReadingService readingService) {
    this.readingService = readingService;
  }

  @PostMapping(Mappings.READING_PAGE + "/{readingId}" + Mappings.DELETE)
  public String showMediumMeter(@PathVariable("readingId") long readingId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
    Long meterId;
    try {
      meterId = readingService.findMediumId(readingId);
    } catch (Exception e) {
      return ControllersHelper
          .redirectToMainPageWithErrorMessageCode(redirectAttributes,
              e.getMessage());
    }
    try {
      readingService.delete(readingId);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }
}
