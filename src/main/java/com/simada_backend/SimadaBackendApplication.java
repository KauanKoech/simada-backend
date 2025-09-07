package com.simada_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.simada_backend")
public class SimadaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimadaBackendApplication.class, args);
	}

}
