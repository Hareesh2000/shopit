package com.shopit.project.service;

import com.shopit.project.payload.OrderDTO;
import com.shopit.project.payload.OrderRequestDTO;

public interface OrderService {
    OrderDTO placeOrder(OrderRequestDTO orderRequestDTO);

    OrderDTO updateOrder(OrderDTO orderDTO, Long orderId);
}
