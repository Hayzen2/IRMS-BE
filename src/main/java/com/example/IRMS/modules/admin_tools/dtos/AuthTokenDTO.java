package com.example.IRMS.modules.admin_tools.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenDTO {
	// Kept internal for AuthService to set cookie; not exposed in API response
	private String accessToken;
	private String tokenType;
	// Public user profile fields
	private String userId;
	private String email;
	private String role;
}
