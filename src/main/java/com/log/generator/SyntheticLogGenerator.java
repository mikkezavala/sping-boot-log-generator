package com.log.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SyntheticLogGenerator {

  public static void main(String[] args) {
    SpringApplication.run(SyntheticLogGenerator.class, args);
  }
}
