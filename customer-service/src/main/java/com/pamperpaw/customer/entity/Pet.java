package com.pamperpaw.customer.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Pet name is required")
    private String name;

    @NotBlank(message = "Pet type is required")
    private String type;

    @Min(value = 0, message = "Age cannot be negative")
    private int age;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

	@ManyToOne
    @JoinColumn(name = "customer_id")
	@JsonBackReference
    private Customer customer;
}
