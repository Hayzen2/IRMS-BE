package com.example.IRMS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IrmsApplication {

	public static void main(String[] args) {
		// Set timezone to UTC before loading any configuration
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		System.setProperty("user.timezone", "UTC");
		
		loadEnvFile();
		SpringApplication.run(IrmsApplication.class, args);
	}
	private static void loadEnvFile() {
        try {
            File envFile = new File(".env");
            if (envFile.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(envFile));
                props.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load .env", e);
        }
    }

}
