package com.pamperpaw.admin.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String address;
}
