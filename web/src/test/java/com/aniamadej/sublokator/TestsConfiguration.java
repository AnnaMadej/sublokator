package com.aniamadej.sublokator;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
}
