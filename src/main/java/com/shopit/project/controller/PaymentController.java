package com.shopit.project.controller;

import com.shopit.project.payload.PaymentDTO;
import com.shopit.project.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PutMapping("/payments/{paymentId}/update/status")
    public ResponseEntity<PaymentDTO> updatePaymentStatus(@Valid @RequestBody PaymentDTO paymentDTO,
                                                                     @PathVariable Long paymentId){
        PaymentDTO updatedPaymentDTO = paymentService.updatePaymentStatus(paymentDTO, paymentId);
        return new ResponseEntity<>(updatedPaymentDTO, HttpStatus.OK);
    }
}
