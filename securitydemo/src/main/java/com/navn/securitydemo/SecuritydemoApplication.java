package com.navn.securitydemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SecuritydemoApplication {

	public static void main(String[] args) {
		log.info("Test");
		SpringApplication.run(SecuritydemoApplication.class, args);
	}

}
