package com.shopit.project.service;

import com.shopit.project.model.CartItem;
import com.shopit.project.payload.CartDTO;
import com.shopit.project.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getUserCart();

    CartDTO updateProductQuantityInCart(Long productId, Integer quantity);

    CartDTO deleteProductFromCart(Long productId);

    Double getCartItemTotalPrice(CartItem cartItem);
}
