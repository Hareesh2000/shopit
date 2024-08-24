package com.shopit.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.project.payload.OrderDTO;
import com.shopit.project.payload.OrderRequestDTO;
import com.shopit.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void testPlaceOrder() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setTransactionId("123456");

        when(orderService.placeOrder(any(OrderRequestDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/api/order")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L));

        verify(orderService, times(1)).placeOrder(any(OrderRequestDTO.class));
    }

    @Test
    void testUpdateOrder() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setTransactionId("123456");

        when(orderService.updateOrder(any(OrderDTO.class), anyLong())).thenReturn(orderDTO);

        mockMvc.perform(put("/api/orders/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L));

        verify(orderService, times(1)).updateOrder(any(OrderDTO.class), eq(1L));
    }
}