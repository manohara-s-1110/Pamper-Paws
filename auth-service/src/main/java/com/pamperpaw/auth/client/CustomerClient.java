package com.pamperpaw.auth.client;

import com.pamperpaw.auth.dto.CustomerProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @PostMapping("/customers")
    Object createCustomer(@RequestBody CustomerProfileRequest request);

    @GetMapping("/customers/username/{username}")
    Object getCustomerByUsername(@PathVariable String username);
}
