package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class ExceptionHandlingController {

  private final MessageSource errorsMessageSource;

  @Autowired
  ExceptionHandlingController(
      MessageSource errorsMessageSource) {
    this.errorsMessageSource = errorsMessageSource;
  }

  @ExceptionHandler(MainException.class)
  public String handleExceptionRedirectingToMainPage(
      Exception e,
      HttpServletRequest request,
      RedirectAttributes redirectAttrs) {

    String errorCode = e.getMessage();
    String errorMessage;
    try {
      errorMessage =
          errorsMessageSource
              .getMessage(errorCode, null, request.getLocale());
    } catch (NoSuchMessageException ex) {
      errorMessage = "error";
    }

    redirectAttrs.addFlashAttribute(Attributes.ERROR, errorMessage);
    return "redirect:" + Mappings.MEDIA_PAGE;
  }


}
