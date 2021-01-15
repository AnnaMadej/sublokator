package com.aniamadej.sublokator;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class ErrorMessageSource extends ReloadableResourceBundleMessageSource {
  public String getMessage(String messageCode) {
    try {
      return this
          .getMessage(messageCode, null, LocaleContextHolder
              .getLocale());
    } catch (NoSuchMessageException e) {
      return "";
    }

  }
}
