package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ShareYourTripAccommodationsMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareYourTripAccommodationsMsApplication.class, args);
	}

}
