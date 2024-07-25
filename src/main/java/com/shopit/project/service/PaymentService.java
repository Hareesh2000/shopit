package com.shopit.project.service;

import com.shopit.project.payload.PaymentDTO;

public interface PaymentService {
    PaymentDTO savePayment(PaymentDTO paymentDTO);

    PaymentDTO updatePaymentStatus(PaymentDTO paymentDTO, Long paymentId);
}
