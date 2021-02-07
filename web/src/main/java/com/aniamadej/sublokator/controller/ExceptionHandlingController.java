package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.Exceptions.InputException;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ExceptionHandlingController {

  @ExceptionHandler(MainException.class)
  public String handleExceptionRedirectingToMainPage(
      Exception e,
      RedirectAttributes redirectAttrs) {

    String errorMessage = e.getMessage();

    redirectAttrs.addFlashAttribute(Attributes.ERROR, errorMessage);
    return "redirect:" + Mappings.CONNECTIONS_PAGE;
  }

  @ExceptionHandler(InputException.class)
  public String handleExceptionRedirectingToSamePage(
      Exception e,
      RedirectAttributes redirectAttrs, HttpServletRequest request) {

    String errorMessage = e.getMessage();

    redirectAttrs.addFlashAttribute(Attributes.ERROR, errorMessage);

    String uri = request.getHeader("referer");
    return "redirect:" + uri;

  }


}
