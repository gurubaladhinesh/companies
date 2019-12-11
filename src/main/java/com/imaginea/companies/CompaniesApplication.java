package com.imaginea.companies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class CompaniesApplication {

  public static void main(String[] args) {
    /*Class[] runner = new Class[]{CompaniesRunner.class};
    System.exit(SpringApplication.exit(SpringApplication.run(runner, args)));*/
    System.exit(SpringApplication.exit(SpringApplication.run(CompaniesApplication.class, args)));
  }


}


