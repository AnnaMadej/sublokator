package com.aniamadej.sublokator.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfiguration {
  @Bean
  public FlywayMigrationStrategy cleanMigrateStrategy() {
    FlywayMigrationStrategy strategy = new FlywayMigrationStrategy() {
      @Override
      public void migrate(Flyway flyway) {
        flyway.repair();
        flyway.migrate();
      }
    };
    return strategy;
  }

}
