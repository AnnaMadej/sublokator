package com.aniamadej.sublokator.configuration;

import com.aniamadej.sublokator.CustomMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
class ErrorMessagesConfiguration {
  @Bean
  public CustomMessageSource errorsMessageSource() {
    CustomMessageSource messageSource
        = new CustomMessageSource();

    messageSource.setBasenames("classpath:messages-errors");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Bean
  public LocalValidatorFactoryBean getValidator() {
    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(errorsMessageSource());
    return bean;
  }
}
