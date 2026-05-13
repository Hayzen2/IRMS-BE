package com.example.IRMS.modules.admin_tools.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.example.IRMS.config.jwt.JwtProvider;
import com.example.IRMS.modules.admin_tools.dtos.AuthTokenDTO;
import com.example.IRMS.modules.admin_tools.models.UserEntity;
import com.example.IRMS.modules.admin_tools.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;

	public AuthTokenDTO login(String email, String rawPassword) {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
		} catch (BadCredentialsException | AuthenticationServiceException ex) {
			throw new BadCredentialsException("Invalid email or password");
		}

		UserEntity user = userRepository.findByEmail(email);
		if (user == null) {
			throw new BadCredentialsException("Invalid email or password");
		}

		String role = "ROLE_" + user.getRole().name();
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", String.valueOf(user.getId()));
		claims.put("email", user.getEmail());
		claims.put("role", role);
		claims.put("roles", java.util.List.of(role));

		String accessToken = jwtProvider.generateAccessToken(claims);

		return AuthTokenDTO.builder()
				.accessToken(accessToken)
				.tokenType("Bearer")
				.userId(String.valueOf(user.getId()))
				.email(user.getEmail())
				.role(role)
				.build();
	}
}
