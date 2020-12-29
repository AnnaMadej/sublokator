package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.CustomMessageSource;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class ExceptionHandlingController {

  private final CustomMessageSource errorsMessageSource;

  @Autowired
  ExceptionHandlingController(
      CustomMessageSource errorsMessageSource) {
    this.errorsMessageSource = errorsMessageSource;
  }

  @ExceptionHandler(MainException.class)
  public String handleExceptionRedirectingToMainPage(
      Exception e,
      RedirectAttributes redirectAttrs) {

    String errorMessage = e.getMessage();

    redirectAttrs.addFlashAttribute(Attributes.ERROR, errorMessage);
    return "redirect:" + Mappings.MEDIA_PAGE;
  }


}
