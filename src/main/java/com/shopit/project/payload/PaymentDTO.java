package com.shopit.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private String gatewayName;
    private String transactionId;
    private String status;
    private String responseMessage;
}
