package com.shopit.project.service;

import com.shopit.project.model.*;
import com.shopit.project.payload.OrderDTO;
import com.shopit.project.payload.OrderRequestDTO;
import com.shopit.project.payload.PaymentDTO;
import com.shopit.project.repository.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void testPlaceOrder() {
        User user = new User();
        Cart cart = new Cart();
        List<CartItem> cartItems = new ArrayList<>();
        CartItem cartItem = new CartItem();
        Product product = new Product();
        product.setProductQuantity(10);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItems.add(cartItem);
        cart.setCartItems(cartItems);
        cart.setTotalPrice(200.0);
        user.setCart(cart);

        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setPaymentMethod("Credit Card");
        orderRequestDTO.setGatewayName("PayPal");
        orderRequestDTO.setTransactionId("12345");
        orderRequestDTO.setPaymentStatus("Success");
        orderRequestDTO.setPaymentResponseMessage("Payment Successful");
        orderRequestDTO.setAddressId(1L);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);

        Order order = new Order();
        Address address = new Address();

        when(authUtil.loggedInUser()).thenReturn(user);
        when(paymentService.savePayment(any(PaymentDTO.class))).thenReturn(paymentDTO);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(new Payment()));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(new OrderDTO());

        OrderDTO result = orderService.placeOrder(orderRequestDTO);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(cart);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testUpdateOrder() {
        Order order = new Order();
        order.setOrderStatus("Processing");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderStatus("Shipped");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

        OrderDTO result = orderService.updateOrder(orderDTO, 1L);

        assertNotNull(result);
        assertEquals("Shipped", result.getOrderStatus());
        verify(orderRepository, times(1)).save(order);
    }
}