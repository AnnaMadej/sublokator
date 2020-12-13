package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.util.Mappings;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

class ControllersHelper {
  public static String redirectToMainPageWithErrorMessageCode(
      RedirectAttributes ra,
      String errorMessageCode) {
    ra.addFlashAttribute("error", errorMessageCode);
    return "redirect:" + Mappings.MEDIA_PAGE;
  }
}
