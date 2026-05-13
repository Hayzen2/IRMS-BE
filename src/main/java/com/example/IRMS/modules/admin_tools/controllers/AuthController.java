package com.example.IRMS.modules.admin_tools.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.IRMS.modules.admin_tools.dtos.AuthTokenDTO;
import com.example.IRMS.modules.admin_tools.dtos.LoginRequestDTO;
import com.example.IRMS.modules.admin_tools.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated // Enable validation for @RequestBody and @RequestHeader
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<AuthTokenDTO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletResponse response) {
		try {
			AuthTokenDTO authData = authService.login(request.email(), request.password());
			// Set JWT in HttpOnly cookie
			// sameSite=Lax for basic CSRF protection, Max-Age=86400 (1 day) 
			// sameSite=Lax stops the cookie from being sent on cross-site requests
			// XSS protection means JavaScript cannot read HttpOnly cookies, but they will still be sent on same-site requests
			// CSRF protection means cookie will not be sent on cross-site requests, they will only be sent on same-site requests
			response.addHeader("Set-Cookie", 
				"access_token=" + authData.getAccessToken() + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + (86400) + "; ");

			// Return user profile only (no token in body)
			authData.setAccessToken(null); 
      authData.setTokenType(null);

			// 3. Return only the safe user profile (userId, email, role)
			return ResponseEntity.ok(authData);
		} catch (BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new AuthTokenDTO(null, null, null, null, null));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
		// Clear cookie by setting Max-Age=0 and empty value
		response.addHeader("Set-Cookie", "access_token=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
		return ResponseEntity.noContent().build();
	}
}
