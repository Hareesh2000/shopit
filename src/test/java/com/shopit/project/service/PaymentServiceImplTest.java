package com.shopit.project.service;

import com.shopit.project.model.Order;
import com.shopit.project.model.Payment;
import com.shopit.project.payload.PaymentDTO;
import com.shopit.project.repository.OrderRepository;
import com.shopit.project.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void testSavePayment() {
        PaymentDTO paymentDTO = new PaymentDTO();
        Payment payment = new Payment();

        when(modelMapper.map(any(PaymentDTO.class), eq(Payment.class))).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(any(Payment.class), eq(PaymentDTO.class))).thenReturn(paymentDTO);

        PaymentDTO savedPaymentDTO = paymentService.savePayment(paymentDTO);

        assertNotNull(savedPaymentDTO);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setStatus("Completed");

        Payment payment = new Payment();
        Order order = new Order();
        payment.setOrder(order);

        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(any(Payment.class), eq(PaymentDTO.class))).thenReturn(paymentDTO);

        PaymentDTO updatedPaymentDTO = paymentService.updatePaymentStatus(paymentDTO, 1L);

        assertNotNull(updatedPaymentDTO);
        assertEquals("Completed", payment.getStatus());
        assertEquals("Processing", order.getOrderStatus());
        verify(paymentRepository).findById(anyLong());
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(any(Order.class));
    }
}