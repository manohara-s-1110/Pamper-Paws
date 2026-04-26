package com.pamperpaw.auth.dto;

import lombok.Data;

@Data
public class CustomerProfileRequest {
    private String username;
    private String name;
    private String email;
    private String phone;
    private String address;
}
