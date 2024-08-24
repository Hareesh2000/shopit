package com.shopit.project.controller;

import com.shopit.project.security.payload.UserInfoResponse;
import com.shopit.project.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testGetUserName() throws Exception {
        String expectedUsername = "testUser";
        String expectedResponse = "Username: " + expectedUsername;

        when(userService.getUsername(authentication)).thenReturn(expectedUsername);

        mockMvc.perform(get("/api/user/username")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        verify(userService, times(1)).getUsername(any(Authentication.class));
    }

    @Test
    void testGetUser() throws Exception {
        UserInfoResponse userInfoResponse = new UserInfoResponse(1L,"testUser",
                new ArrayList<>(List.of("ROLE_USER")));
        userInfoResponse.setUserId(1L);
        userInfoResponse.setUsername("testUser");
        when(userService.getUser(any(Authentication.class))).thenReturn(userInfoResponse);

        mockMvc.perform(get("/api/user")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(userService, times(1)).getUser(any(Authentication.class));
    }
}