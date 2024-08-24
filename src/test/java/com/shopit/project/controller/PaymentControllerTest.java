package com.shopit.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.project.payload.PaymentDTO;
import com.shopit.project.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void testUpdatePaymentStatus() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);
        when(paymentService.updatePaymentStatus(any(PaymentDTO.class), anyLong())).thenReturn(paymentDTO);

        mockMvc.perform(put("/api/payments/{paymentId}/update/status",1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paymentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1L));

        verify(paymentService, times(1)).updatePaymentStatus(any(PaymentDTO.class), eq(1L));
    }
}