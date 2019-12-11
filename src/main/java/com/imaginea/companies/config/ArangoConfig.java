package com.imaginea.companies.config;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDB.Builder;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableArangoRepositories(basePackages = {"com.imaginea.companies"})
@PropertySource("classpath:arango.properties")
public class ArangoConfig implements ArangoConfiguration {

  @Value("${arango.host}")
  String host;

  @Value("${arango.port}")
  Integer port;

  @Value("${arango.userName}")
  String userName;

  @Value("${arango.password}")
  String password;

  @Value("${arango.maxConnections}")
  Integer maxConnections;

  @Value("${arango.db}")
  String database;

  @Override
  public Builder arango() {
    return new ArangoDB.Builder().host(host, port).user(userName).password(password)
        .maxConnections(maxConnections);
  }

  @Override
  public String database() {
    return database;
  }

}
