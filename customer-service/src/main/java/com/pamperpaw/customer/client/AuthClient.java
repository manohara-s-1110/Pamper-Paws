package com.pamperpaw.customer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @DeleteMapping("/auth/username/{username}")
    String deleteUser(@PathVariable String username);
}