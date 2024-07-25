package com.shopit.project.service;

import com.shopit.project.exceptions.ResourceNotFoundException;
import com.shopit.project.model.Order;
import com.shopit.project.model.Payment;
import com.shopit.project.payload.PaymentDTO;
import com.shopit.project.repository.OrderRepository;
import com.shopit.project.repository.PaymentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final ModelMapper modelMapper;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(ModelMapper modelMapper, PaymentRepository paymentRepository,
                              OrderRepository orderRepository) {
        this.modelMapper = modelMapper;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public PaymentDTO savePayment(PaymentDTO paymentDTO) {
        Payment payment = modelMapper.map(paymentDTO, Payment.class);

        Payment savedPayment = paymentRepository.save(payment);

        return modelMapper.map(savedPayment, PaymentDTO.class);
    }

    @Override
    public PaymentDTO updatePaymentStatus(PaymentDTO paymentDTO, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        payment.setStatus(paymentDTO.getStatus());

        Order order = payment.getOrder();
        order.setOrderStatus("Processing");

        Payment savedPayment = paymentRepository.save(payment);
        orderRepository.save(order);

        return modelMapper.map(savedPayment, PaymentDTO.class);
    }
}
