package com.rchilli.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BrokenLinkDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokenLinkDetectorApplication.class, args);
	}

}
