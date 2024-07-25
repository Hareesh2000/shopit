package com.shopit.project.controller;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.model.Cart;
import com.shopit.project.payload.CartDTO;
import com.shopit.project.payload.ProductDTO;
import com.shopit.project.security.payload.MessageResponse;
import com.shopit.project.security.services.UserDetailsImpl;
import com.shopit.project.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_USER')")
    @PostMapping("/cart/products/{productId}/add/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
                                              @PathVariable Integer quantity){

        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts(){
        List<CartDTO> cartDTOS = cartService.getAllCarts();
        return new ResponseEntity<List<CartDTO>>(cartDTOS, HttpStatus.FOUND);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_USER')")
    @GetMapping("/cart")
    public ResponseEntity<CartDTO> getUserCart(){
        CartDTO cartDTO = cartService.getUserCart();
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.FOUND);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_USER')")
    @PostMapping("/cart/products/{productId}/update/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateProductQuantityInCart(@PathVariable Long productId,
                                                    @PathVariable Integer quantity){
        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, quantity);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SELLER', 'ROLE_USER')")
    @DeleteMapping("/cart/products/{productId}")
    public ResponseEntity<CartDTO> deleteProductFromCart(@PathVariable Long productId){
        CartDTO cartDTO = cartService.deleteProductFromCart(productId);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

}
