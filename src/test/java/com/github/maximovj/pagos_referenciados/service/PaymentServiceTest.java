package com.github.maximovj.pagos_referenciados.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.github.maximovj.pagos_referenciados.model.Payment;
import com.github.maximovj.pagos_referenciados.repository.IPaymentRepository;
import com.github.maximovj.pagos_referenciados.request.PaymentRequest;
import com.github.maximovj.pagos_referenciados.response.ApiResponse;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockBean
    private IPaymentRepository paymentRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testSavePayment_WithCallback() {
        PaymentRequest request = new PaymentRequest();
        request.setExternalId("EXT-101");
        request.setAmount(1500.0);
        request.setDescription("Pago con callback");
        request.setCallbackURL("http://mock.callback");

        // Mockear repository.save
        Payment savedPayment = new Payment();
        savedPayment.setPaymentId(1L);
        savedPayment.setReference("REF-101");
        savedPayment.setStatus("01");
        savedPayment.setAmount(request.getAmount()); 
        savedPayment.setCreationDate(LocalDateTime.now()); 
        savedPayment.setPaymentDate(LocalDateTime.now());
        Mockito.when(paymentRepository.save(Mockito.any(Payment.class))).thenReturn(savedPayment);

        // Mockear RestTemplate para devolver ACK
        Map<String, String> ackResponse = new HashMap<>();
        ackResponse.put("status", "ACK");
        ackResponse.put("acknowledgeId", "ACK-123");
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(ackResponse);

        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.eq(Map.class)))
                .thenReturn(responseEntity);

        ResponseEntity<ApiResponse> response = paymentService.savePayment(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Payment created successfully", response.getBody().getResponse_message());
        assertEquals("REF-101", response.getBody().getData().get("reference"));
    }
}
