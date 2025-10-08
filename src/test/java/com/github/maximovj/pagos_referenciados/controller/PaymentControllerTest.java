package com.github.maximovj.pagos_referenciados.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximovj.pagos_referenciados.model.Payment;
import com.github.maximovj.pagos_referenciados.repository.IPaymentRepository;
import com.github.maximovj.pagos_referenciados.request.PaymentCallBackRequest;
import com.github.maximovj.pagos_referenciados.request.PaymentCancelRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IPaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${jwt.app.token}")
    private String appToken;

    @BeforeEach
    void setup() {
        paymentRepository.deleteAll();

        Payment p1 = new Payment();
        p1.setExternalId("EXT-001");
        p1.setAmount(1500.0);
        p1.setDescription("Pago de servicio A");
        p1.setReference("PRV3E9352BE0F1A4C99A92B17B5438");
        p1.setAuthorizationNumber("456789");
        p1.setStatus("01");
        p1.setCreationDate(LocalDateTime.now().minusDays(2));
        p1.setPaymentDate(LocalDateTime.now().minusDays(1));
        p1.setCallbackACKID("ORD-12345");
        paymentRepository.save(p1);

        Payment p2 = new Payment();
        p2.setExternalId("EXT-002");
        p2.setAmount(2000.0);
        p2.setDescription("Pago de servicio B");
        p2.setReference("PRV4B7352BE0F1A4C00A92B17B5438");
        p2.setAuthorizationNumber("678123");
        p2.setStatus("02");
        p2.setCreationDate(LocalDateTime.now());
        p2.setPaymentDate(LocalDateTime.now());
        p2.setCallbackACKID("ORD-45678");
        paymentRepository.save(p2);

        Payment p3 = new Payment();
        p3.setExternalId("EXT-003");
        p3.setAmount(2300.0);
        p3.setDescription("Pago de servicio C");
        p3.setReference("PRV5C8352BE0F1A4C55A92B17B5438");
        p3.setAuthorizationNumber("789801");
        p3.setStatus("01");
        p3.setCreationDate(LocalDateTime.now());
        p3.setPaymentDate(LocalDateTime.now());
        p3.setCallbackACKID("ORD-89349");
        paymentRepository.save(p3);
    }

    @Test
    void testGetPayment_Success() throws Exception {
        // Tomamos un payment que ya guardaste en @BeforeEach
        Payment payment = paymentRepository.findAll().get(0); 
        String reference = payment.getReference();
        String paymentId = payment.getPaymentId().toString();

        mockMvc.perform(get("/api/v1/payment/{reference}/{paymentId}", reference, paymentId)
                        .header("Authorization", "Bearer " + appToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_code").value(200))
                .andExpect(jsonPath("$.response_message").value("Payment verified successfully"))
                .andExpect(jsonPath("$.data.reference").value(reference))
                .andExpect(jsonPath("$.data.paymentId").value(paymentId));
    }

    @Test
    void testGetAllPayments_Success() throws Exception {
        String startCreationDate = LocalDateTime.now().minusDays(3).format(formatter);
        String endCreationDate = LocalDateTime.now().plusDays(1).format(formatter);
        String startPaymentDate = LocalDateTime.now().minusDays(3).format(formatter);
        String endPaymentDate = LocalDateTime.now().plusDays(1).format(formatter);

        mockMvc.perform(get("/api/v1/payments/search")
                        .param("startCreationDate", startCreationDate)
                        .param("endCreationDate", endCreationDate)
                        .param("startPaymentDate", startPaymentDate)
                        .param("endPaymentDate", endPaymentDate)
                        .param("status", "01")
                        .header("Authorization", "Bearer " + appToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_code").value(200))
                .andExpect(jsonPath("$.response_message").value("Payments retrieved successfully"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testGetAllPayments_NoResults() throws Exception {
        String startCreationDate = LocalDateTime.now().minusDays(10).format(formatter);
        String endCreationDate = LocalDateTime.now().minusDays(9).format(formatter);
        String startPaymentDate = LocalDateTime.now().minusDays(10).format(formatter);
        String endPaymentDate = LocalDateTime.now().minusDays(9).format(formatter);

        mockMvc.perform(get("/api/v1/payments/search")
                        .param("startCreationDate", startCreationDate)
                        .param("endCreationDate", endCreationDate)
                        .param("startPaymentDate", startPaymentDate)
                        .param("endPaymentDate", endPaymentDate)
                        .param("status", "99")
                        .header("Authorization", "Bearer " + appToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testCancelPayment_Success() throws Exception {
        PaymentCancelRequest request = new PaymentCancelRequest();
        request.setReference("PRV3E9352BE0F1A4C99A92B17B5438");
        request.setStatus("03");
        request.setUpdateDescription("Cancelación solicitada por cliente");

        mockMvc.perform(put("/api/v1/payment/cancel")
                        .header("Authorization", "Bearer " + appToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_code").value(200))
                .andExpect(jsonPath("$.response_message").value("Payment canceled successfully"))
                .andExpect(jsonPath("$.data.reference").value("PRV3E9352BE0F1A4C99A92B17B5438"))
                .andExpect(jsonPath("$.data.status").value("03"))
                .andExpect(jsonPath("$.data.cancelDescription").value("Cancelación solicitada por cliente"));
    }

    @Test
    void testCancelPayment_ConflictStatus() throws Exception {
        PaymentCancelRequest request = new PaymentCancelRequest();
        request.setReference("PRV4B7352BE0F1A4C00A92B17B5438");
        request.setStatus("03");
        request.setUpdateDescription("Intento de cancelación inválido");

        mockMvc.perform(put("/api/v1/payment/cancel")
                        .header("Authorization", "Bearer " + appToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_code").value(409))
                .andExpect(jsonPath("$.response_message").value("Payment not canceled"));
    }

    @Test
    void testCancelPayment_NotFound() throws Exception {
        PaymentCancelRequest request = new PaymentCancelRequest();
        request.setReference("PRV0E0000BE0F0A0C00A00B00B0000");
        request.setStatus("03");
        request.setUpdateDescription("Referencia no encontrada");

        mockMvc.perform(put("/api/v1/payment/cancel")
                        .header("Authorization", "Bearer " + appToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_code").value(404))
                .andExpect(jsonPath("$.response_message").value("Payment not found"));
    }

    @Test
    void testCallbackPayment_Success() throws Exception {
        // Tomamos un payment que ya guardaste en @BeforeEach
        Payment payment = paymentRepository.findAll().get(0);

        PaymentCallBackRequest request = new PaymentCallBackRequest();
        request.setPaymentId(payment.getPaymentId());
        request.setAmount(payment.getAmount());
        request.setPaymentDate(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setReference(payment.getReference());
        request.setExternalId(payment.getExternalId());
        request.setAuthorizationNumber(payment.getAuthorizationNumber());
        request.setStatus(payment.getStatus());

        mockMvc.perform(post("/api/v1/payment/callback")
                        .header("Authorization", "Bearer " + appToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledgeId").exists())
                .andExpect(jsonPath("$.status").value("ACK"));
    }

}