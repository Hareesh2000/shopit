package com.shopit.project.controller;

import com.shopit.project.payload.CartDTO;
import com.shopit.project.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    void testAddProductToCart() throws Exception {
        CartDTO cartDTO = new CartDTO();

        when(cartService.addProductToCart(anyLong(), anyInt())).thenReturn(cartDTO);

        mockMvc.perform(post("/api/cart/products/1/add/quantity/2"))
                .andExpect(status().isCreated());

        verify(cartService, times(1)).addProductToCart(1L, 2);
    }

    @Test
    void testGetAllCarts() throws Exception {
        List<CartDTO> cartDTOS = new ArrayList<>();
        when(cartService.getAllCarts()).thenReturn(cartDTOS);

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.length()").value(cartDTOS.size()));

        verify(cartService, times(1)).getAllCarts();
    }

    @Test
    void testGetUserCart() throws Exception {
        CartDTO cartDTO = new CartDTO();
        when(cartService.getUserCart()).thenReturn(cartDTO);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isFound());

        verify(cartService, times(1)).getUserCart();
    }

    @Test
    void testUpdateProductQuantityInCart() throws Exception {
        CartDTO cartDTO = new CartDTO();

        when(cartService.updateProductQuantityInCart(anyLong(), anyInt())).thenReturn(cartDTO);

        mockMvc.perform(post("/api/cart/products/1/update/quantity/5"))
                .andExpect(status().isCreated());

        verify(cartService, times(1)).updateProductQuantityInCart(1L, 5);
    }

    @Test
    void testDeleteProductFromCart() throws Exception {
        CartDTO cartDTO = new CartDTO();

        when(cartService.deleteProductFromCart(anyLong())).thenReturn(cartDTO);

        mockMvc.perform(delete("/api/cart/products/1"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).deleteProductFromCart(1L);
    }
}