package com.example.IRMS.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.IRMS.modules.admin_tools.enums.PermissionType;
import com.example.IRMS.modules.admin_tools.enums.RoleType;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {
    private final String id;
    private final String email;
    private final String password;
    private final RoleType role;
    private final Set<PermissionType> permissions;

    public CustomUserDetails(String id, String email, String password, RoleType role, Set<PermissionType> permissions) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public CustomUserDetails(String id, String email, String password, RoleType role) {
        this(id, email, password, role, Set.of());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Keep role authority for role-based checks.
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // Add direct permission authorities for fine-grained checks.
        for (PermissionType permission : permissions) {
            authorities.add(new SimpleGrantedAuthority("PERM_" + permission.name()));
        }

        return authorities;
    }
    
    @Override
    public String getUsername() {
        return email; // Use email as the username for authentication
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account never expires
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials never expire
    }
}
