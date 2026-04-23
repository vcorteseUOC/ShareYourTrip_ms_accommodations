package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.integration;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "user-service",
        url = "${services.users.base-url}"
)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/lan/{language}")
    List<Long> getUserIdsByLanguage(@PathVariable("language") String language);
}