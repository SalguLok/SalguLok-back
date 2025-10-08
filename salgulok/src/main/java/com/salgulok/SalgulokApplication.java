package com.salgulok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class SalgulokApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalgulokApplication.class, args);
	}

}
