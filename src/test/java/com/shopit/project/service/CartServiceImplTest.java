package com.shopit.project.service;

import com.shopit.project.model.Cart;
import com.shopit.project.model.CartItem;
import com.shopit.project.model.Product;
import com.shopit.project.model.User;
import com.shopit.project.payload.CartDTO;
import com.shopit.project.payload.CartItemDTO;
import com.shopit.project.payload.ProductDTO;
import com.shopit.project.repository.CartItemRepository;
import com.shopit.project.repository.CartRepository;
import com.shopit.project.repository.ProductRepository;
import com.shopit.project.util.AuthUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthUtil authUtil;

    @Test
    void testAddProductToCart() {
        User user = new User();
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());
        cart.setTotalPrice(0.0);
        user.setCart(cart);

        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setProductPrice(100.0);
        cartItemDTO.setQuantity(1);
        cartItemDTO.setProductDiscountPercentage(10.0);
        cartItemDTO.setTotalPrice(90.0);

        Product product = new Product();
        product.setProductId(1L);
        product.setProductQuantity(10);
        product.setProductName("Test Product");
        product.setProductPrice(100.0);
        product.setProductDiscountPercentage(10.0);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        cartItemDTO.setProduct(productDTO);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(authUtil.loggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(cartItemRepository.findCartItemByProductAndCart(product, cart)).thenReturn(null);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());
        when(modelMapper.map(any(CartItem.class), eq(CartItemDTO.class))).thenReturn(cartItemDTO);

        CartDTO result = cartService.addProductToCart(1L, 2);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testGetAllCarts() {
        List<Cart> carts = new ArrayList<>();
        carts.add(new Cart());
        when(cartRepository.findAll()).thenReturn(carts);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());

        List<CartDTO> result = cartService.getAllCarts();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetUserCart() {
        User user = new User();
        Cart cart = new Cart();
        user.setCart(cart);
        when(authUtil.loggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());

        CartDTO result = cartService.getUserCart();

        assertNotNull(result);
    }

    @Test
    void testUpdateProductQuantityInCart() {
        User user = new User();
        Cart cart = new Cart();
        cart.setTotalPrice(200.0);
        user.setCart(cart);

        Product product = new Product();
        product.setProductId(1L);
        product.setProductQuantity(10);
        product.setProductName("Test Product");
        product.setProductPrice(100.0);
        product.setProductDiscountPercentage(10.0);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setProductPrice(100.0);
        cartItem.setProductDiscountPercentage(10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(authUtil.loggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findCartItemByProductAndCart(product, cart)).thenReturn(cartItem);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());

        CartDTO result = cartService.updateProductQuantityInCart(1L, 5);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testDeleteProductFromCart() {
        User user = new User();
        Cart cart = new Cart();
        cart.setTotalPrice(200.0);
        user.setCart(cart);

        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("Test Product");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setProductPrice(100.0);
        cartItem.setProductDiscountPercentage(10.0);

        List<CartItem> cartItems = new ArrayList<>(List.of(cartItem));
        cart.setCartItems(cartItems);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(authUtil.loggedInUser()).thenReturn(user);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(cartItemRepository.findCartItemByProductAndCart(product, cart)).thenReturn(cartItem);
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());

        CartDTO result = cartService.deleteProductFromCart(1L);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(cart);
        assertTrue(cart.getCartItems().isEmpty());
    }
}