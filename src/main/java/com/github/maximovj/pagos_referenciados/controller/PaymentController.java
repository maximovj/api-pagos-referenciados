package com.github.maximovj.pagos_referenciados.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximovj.pagos_referenciados.model.Payment;
import com.github.maximovj.pagos_referenciados.request.PaymentCallBackRequest;
import com.github.maximovj.pagos_referenciados.request.PaymentCancelRequest;
import com.github.maximovj.pagos_referenciados.request.PaymentRequest;
import com.github.maximovj.pagos_referenciados.response.ApiResponse;
import com.github.maximovj.pagos_referenciados.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.savePayment(request);
    }

    @GetMapping("/payment/{reference}/{paymentId}")
    public ResponseEntity<ApiResponse> getPayment(
            @PathVariable String reference,
            @PathVariable String paymentId) {
        return paymentService.findPayment(reference, paymentId);
    }

    @GetMapping("/payments/search")
    public ResponseEntity<ApiResponse> getAllPayments(
            @RequestParam(required = false) String startCreationDate,
            @RequestParam(required = false) String endCreationDate,
            @RequestParam(required = false) String startPaymentDate,
            @RequestParam(required = false) String endPaymentDate,
            @RequestParam(required = false) String status) {
        return paymentService.getAllPaymentsPaged(startCreationDate, endCreationDate,
                startPaymentDate, endPaymentDate, status);
    }

    @PutMapping("/payment/cancel")
    public ResponseEntity<ApiResponse> updatePayment(@Valid @RequestBody PaymentCancelRequest request) {
        return paymentService.updatePayment(request);
    }

    @PostMapping("/payment/callback")
    public ResponseEntity<Map<String, Object>> callbackPayment(@Valid @RequestBody PaymentCallBackRequest request) {
        return paymentService.callbackPayment(request);
    }

}
