package com.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Init {

	public static void main(String[] args) {
System.out.println(System.getenv("SECRET"));
		SpringApplication.run(Init.class, args);
	}
}
