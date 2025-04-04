package com.example.exrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class ExRateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExRateApplication.class, args);
	}
}
