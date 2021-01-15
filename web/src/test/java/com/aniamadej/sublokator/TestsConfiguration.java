package com.aniamadej.sublokator;

import javax.sql.DataSource;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
class TestsConfiguration {

  @Bean
  @Primary
  public DataSource getDataSource() {
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("org.h2.Driver");
    dataSourceBuilder.url("jdbc:h2:mem:test");
    dataSourceBuilder.username("sa");
    dataSourceBuilder.password("");
    DataSource datasource = dataSourceBuilder.build();

    return datasource;
  }

  @Bean
  @Primary
  public TestRestTemplate testRestTemplate() {

    // configuration in order to make testRestTemplate follow page redirecting

    HttpComponentsClientHttpRequestFactory factory =
        new HttpComponentsClientHttpRequestFactory();
    HttpClient httpClient = HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy())
        .build();
    factory.setHttpClient(httpClient);

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    testRestTemplate.getRestTemplate().setRequestFactory(factory);
    return testRestTemplate;
  }

}
