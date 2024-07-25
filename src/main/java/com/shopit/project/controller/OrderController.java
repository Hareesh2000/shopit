package com.shopit.project.controller;

import com.shopit.project.payload.OrderDTO;
import com.shopit.project.payload.OrderRequestDTO;
import com.shopit.project.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order")
    public ResponseEntity<OrderDTO> placeOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        OrderDTO newOrderDTO = orderService.placeOrder(orderRequestDTO);
        return new ResponseEntity<>(newOrderDTO, HttpStatus.CREATED);
    }

    @PutMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@Valid @RequestBody OrderDTO orderDTO, @PathVariable Long orderId){
        OrderDTO updatedOrderDTO = orderService.updateOrder(orderDTO, orderId);
        return new ResponseEntity<>(updatedOrderDTO, HttpStatus.OK);
    }
}
