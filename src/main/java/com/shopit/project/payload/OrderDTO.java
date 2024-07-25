package com.shopit.project.payload;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.shopit.project.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private List<OrderItemDTO> orderItems;
    private PaymentDTO payment;
    private AddressDTO address;
    private Double totalPrice;
    private String orderStatus;
    private LocalDate orderDate;
}
