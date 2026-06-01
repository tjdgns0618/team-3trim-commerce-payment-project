package com.example.team3trimcommercepaymentproject.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public record LoginRequest(
	@Email
	String email,
	@NotBlank
	String password
) {}
