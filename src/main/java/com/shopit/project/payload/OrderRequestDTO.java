package com.shopit.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Long addressId;
    private String paymentMethod;
    private String gatewayName;
    private String transactionId;
    private String paymentStatus;
    private String paymentResponseMessage;
}
