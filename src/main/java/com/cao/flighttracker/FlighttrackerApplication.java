package com.cao.flighttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FlighttrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlighttrackerApplication.class, args);
	}

}
