package com.shopit.project.config;

import com.shopit.project.security.payload.AuthResponse;
import com.shopit.project.security.payload.SignupRequest;
import com.shopit.project.security.payload.SignupResponse;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public SignupResponse signupResponse() {
        return new SignupResponse();
    }

    @Bean
    public SignupRequest signupRequest() {
        return new SignupRequest();
    }

    @Bean
    public AuthResponse authResponse() {
        return new AuthResponse();
    }
}
