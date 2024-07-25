package com.shopit.project.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.exceptions.ResourceNotFoundException;
import com.shopit.project.model.*;
import com.shopit.project.payload.OrderDTO;
import com.shopit.project.payload.OrderRequestDTO;
import com.shopit.project.payload.PaymentDTO;
import com.shopit.project.repository.*;
import com.shopit.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.hibernate.sql.Update;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {
    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;
    private final AuthUtil authUtil;
    private final PaymentService paymentService;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(ModelMapper modelMapper, AddressRepository addressRepository,
                            AuthUtil authUtil, PaymentService paymentService, OrderItemRepository orderItemRepository, PaymentRepository paymentRepository, OrderRepository orderRepository, CartRepository cartRepository, ProductRepository productRepository) {
        this.modelMapper = modelMapper;
        this.addressRepository = addressRepository;
        this.authUtil = authUtil;
        this.paymentService = paymentService;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    private void updateProductStock(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        for(CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer availableStock = product.getProductQuantity();
            Integer requestStock = cartItem.getQuantity();
            if(requestStock > availableStock)
                throw new APIException("Stock Not Available for the Product " +
                        product.getProductName() + " for the quantity " + requestStock +
                        ". Available stock is " + availableStock);

            product.setProductQuantity(availableStock - requestStock);
            productRepository.save(product);
        }
    }

    @Transactional
    @Override
    public OrderDTO placeOrder(OrderRequestDTO orderRequestDTO) {
        Order order = new Order();

        User user = authUtil.loggedInUser();
        order.setUser(user);

        Cart cart = user.getCart();
        List<CartItem> cartsItems = cart.getCartItems();

        if(cartsItems.isEmpty())
            throw new APIException("No Item has been added to the Cart!");

        updateProductStock(cart);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartsItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProductPrice(cartItem.getProductPrice());
            orderItem.setProductDiscountPercentage(cartItem.getProductDiscountPercentage());
            orderItems.add(orderItem);
        }

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);
        order.setOrderItems(savedOrderItems);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentMethod(orderRequestDTO.getPaymentMethod());
        paymentDTO.setGatewayName(orderRequestDTO.getGatewayName());
        paymentDTO.setTransactionId(orderRequestDTO.getTransactionId());
        paymentDTO.setStatus(orderRequestDTO.getPaymentStatus());
        paymentDTO.setResponseMessage(orderRequestDTO.getPaymentResponseMessage());

        PaymentDTO savedPaymentDTO = paymentService.savePayment(paymentDTO);

        Payment payment = paymentRepository.findById(savedPaymentDTO.getPaymentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", "Payment ID",
                                savedPaymentDTO.getPaymentId()));
        order.setPayment(payment);

        Address address = addressRepository.findById(orderRequestDTO.getAddressId())
                        .orElseThrow(() -> new ResourceNotFoundException("Address", "Address id",
                                orderRequestDTO.getAddressId()));

        order.setAddress(address);

        order.setTotalPrice(cart.getTotalPrice());

        if(Objects.equals(payment.getStatus(), "Failed"))
            order.setOrderStatus("Awaiting Payment");

        order.setOrderStatus("Processing");

        order.setOrderDate(LocalDate.now());

        Order savedOrder = orderRepository.save(order);


        payment.setOrder(savedOrder);
        paymentRepository.save(payment);

        for (OrderItem orderItem : savedOrderItems) {
            orderItem.setOrder(savedOrder);
        }
        orderItemRepository.saveAll(orderItems);

        cart.getCartItems().clear(); //Clear Cart
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Override
    public OrderDTO updateOrder(OrderDTO orderDTO, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setOrderStatus(orderDTO.getOrderStatus());
        orderRepository.save(order);

        return modelMapper.map(order, OrderDTO.class);
    }


}
