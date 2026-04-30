package com.example.IRMS.utils;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.IRMS.modules.admin_tools.enums.RoleType;
import com.example.IRMS.modules.admin_tools.models.UserEntity;
import com.example.IRMS.modules.admin_tools.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        createManagerIfNotExists();
        createChefIfNotExists();
        createServerIfNotExists();
        createCashierIfNotExists();
    }

    private void createManagerIfNotExists() {
        if (userRepository.existsByEmail("manager@irms.com")) return;
    
        UserEntity manager = new UserEntity();

        manager.setName("System Manager");
        manager.setEmail("manager@irms.com");
        manager.setRole(RoleType.MANAGER);
      
        manager.setHashedPassword(passwordEncoder.encode("manager123"));

        userRepository.save(manager);
    }
    private void createChefIfNotExists() {
        if (userRepository.existsByEmail("chef@irms.com")) return;
    
        UserEntity chef = new UserEntity();
        chef.setName("System Chef");
        chef.setEmail("chef@irms.com");
        chef.setRole(RoleType.CHEF);
        chef.setHashedPassword(passwordEncoder.encode("chef123"));

        userRepository.save(chef);
    }

    private void createServerIfNotExists() {
        if (userRepository.existsByEmail("server@irms.com")) return;
    
        UserEntity server = new UserEntity();
        server.setName("System Server");
        server.setEmail("server@irms.com");
        server.setRole(RoleType.SERVER);
      
        server.setHashedPassword(passwordEncoder.encode("server123"));

        userRepository.save(server);
    }

    private void createCashierIfNotExists() {
        if (userRepository.existsByEmail("cashier@irms.com")) return;
    
        UserEntity cashier = new UserEntity();
        cashier.setName("System Cashier");
        cashier.setEmail("cashier@irms.com");
        cashier.setRole(RoleType.CASHIER);
      
        cashier.setHashedPassword(passwordEncoder.encode("cashier123"));

        userRepository.save(cashier);
    }
}
