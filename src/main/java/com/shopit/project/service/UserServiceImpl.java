package com.shopit.project.service;

import com.shopit.project.exceptions.AuthenticationException;
import com.shopit.project.security.payload.UserInfoResponse;
import com.shopit.project.security.model.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public String getUsername(Authentication authentication) {
        if(authentication == null)
            throw new AuthenticationException("Error: Not Authenticated");
        return authentication.getName();
    }

    @Override
    public UserInfoResponse getUser(Authentication authentication) {
        if(authentication == null)
            throw new AuthenticationException("Error: Not Authenticated");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new UserInfoResponse(userDetails.getUserId(), userDetails.getUsername(), roles);
    }
}
