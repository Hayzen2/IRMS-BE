package com.example.IRMS.modules.admin_tools.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.IRMS.config.security.CustomUserDetails;
import com.example.IRMS.modules.admin_tools.models.UserEntity;
import com.example.IRMS.modules.admin_tools.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final StaffManagementService rbacService;

    // AuthenticationManager looks for a UserDetailsService
    // UserDetailsService is a Spring Security interface that defines a method to load user-specific data
    @Override 
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Convert user to CustomUserDetails
        return new CustomUserDetails(
            String.valueOf(user.getId()),
            user.getEmail(),
            user.getHashedPassword(),
            user.getRole(),
            rbacService.permissionsForRole(user.getRole())
        );
    }
}
