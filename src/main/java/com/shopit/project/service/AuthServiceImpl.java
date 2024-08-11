package com.shopit.project.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.exceptions.AuthenticationException;
import com.shopit.project.model.Cart;
import com.shopit.project.model.Role;
import com.shopit.project.model.RoleType;
import com.shopit.project.model.User;
import com.shopit.project.repository.CartRepository;
import com.shopit.project.repository.RoleRepository;
import com.shopit.project.repository.UserRepository;
import com.shopit.project.security.jwt.JwtUtils;
import com.shopit.project.security.payload.*;
import com.shopit.project.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final CartRepository cartRepository;

    private final SignupResponse signupResponse;

    @Autowired
    public AuthServiceImpl(JwtUtils jwtUtils, AuthenticationManager authenticationManager,
                           UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, CartRepository cartRepository,
                           SignupResponse signupResponse) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.cartRepository = cartRepository;
        this.signupResponse = signupResponse;
    }

    @Override
    public SignupResponse registerUser(SignupRequest signupRequest) {

        if (userRepository.existsByUserName(signupRequest.getUsername())) {
            signupResponse.setSuccess("false");
            signupResponse.setMessage("Error: Username is already taken!");
            return signupResponse;
        }

        if (userRepository.existsByUserEmail(signupRequest.getEmail())) {
            signupResponse.setSuccess("false");
            signupResponse.setMessage("Error: Email is already in use!");
            return signupResponse;
        }

        // Create new user's account
        User user = new User(signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()));

        String roleRequested = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        Role userRole = roleRepository.findByRoleName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: User Role is not found."));

        Role sellerRole = roleRepository.findByRoleName(RoleType.ROLE_SELLER)
                .orElseThrow(() -> new RuntimeException("Error: Seller Role is not found."));

        Role adminRole = roleRepository.findByRoleName(RoleType.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Admin Role is not found."));

        if (roleRequested == null){
            roles.add(userRole);
        }
        else {
            switch (roleRequested) {
                case "admin":
                    roles.add(adminRole);
                    roles.add(userRole);
                    roles.add(sellerRole);
                    break;

                case "seller":
                    roles.add(sellerRole);
                    roles.add(userRole);
                    break;

                case "user":
                    roles.add(userRole);
                    break;

                default:
                    throw new APIException("Unknown role: " + roleRequested);
            }
        }

        user.setRoles(roles);
        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        user.setCart(cart);

        cartRepository.save(cart);
        cart.setUser(user);
        userRepository.save(user);

        signupResponse.setSuccess("true");
        signupResponse.setMessage("User registered successfully!");
        return signupResponse;
    }

    @Override
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            throw new AuthenticationException("Error: Bad credentials!");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new AuthResponse(userDetails.getUserId(),
                userDetails.getUsername(), roles, jwtCookie.toString());
    }

    @Override
    public ResponseCookie generateLogoutCookie() {
        return jwtUtils.generateCleanJwtCookie();
    }
}
