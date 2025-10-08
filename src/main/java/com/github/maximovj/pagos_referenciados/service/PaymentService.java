package com.github.maximovj.pagos_referenciados.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.maximovj.pagos_referenciados.model.Payment;
import com.github.maximovj.pagos_referenciados.repository.IPaymentRepository;
import com.github.maximovj.pagos_referenciados.request.PaymentCallBackRequest;
import com.github.maximovj.pagos_referenciados.request.PaymentCancelRequest;
import com.github.maximovj.pagos_referenciados.request.PaymentRequest;
import com.github.maximovj.pagos_referenciados.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private IPaymentRepository paymentRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${jwt.app.token}")
    private String appToken;

    // ! Creación de Pago Referenciado
    public ResponseEntity<ApiResponse> savePayment(PaymentRequest request) {

        Payment payment = new Payment();
        payment.setExternalId(request.getExternalId());
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setCallbackURL(request.getCallbackURL());
        payment.setDueDate(request.getDueDate());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCreationDate(LocalDateTime.now());
        payment.setStatus("01");

        if (paymentRepository.existsByReference(payment.getReference())) {
            return this.buildError(HttpStatus.CONFLICT.value(), "La referencia ya existe");
        }

        Payment savedPayment = this.createPayment(payment, request);

        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", savedPayment.getPaymentId().toString());
        data.put("reference", savedPayment.getReference());
        data.put("amount", savedPayment.getAmount().toString());
        data.put("description", savedPayment.getDescription());
        data.put("creationDate",
                savedPayment.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("status", savedPayment.getStatus());
        data.put("message", "Payment created successfully");

        return this.buildSuccess(data, "Payment created successfully");
    }

    public Payment createPayment(Payment payment, PaymentRequest request) {
        // Guardar el pago
        Payment savedPayment = paymentRepository.save(payment);

        // Enviar callback si existe callbackURL
        log.info("Pago referido guardado ::: " + savedPayment.toString());
        sendCallback(savedPayment, request);

        return savedPayment;
    }

    private void sendCallback(Payment payment, PaymentRequest request) {
        if (payment.getCallbackURL() == null || payment.getCallbackURL().isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", payment.getPaymentId());
        payload.put("externalId", payment.getExternalId());
        payload.put("amount", payment.getAmount());
        payload.put("authorizationNumber", payment.getAuthorizationNumber());
        payload.put("reference", payment.getReference());
        payload.put("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        payload.put("status", payment.getStatus());
        payload.put("message", "Payment processed successfully");

        log.info("Payload a enviar: " + payload);

        int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {

                // Crea los headers y agrega el Bearer token
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + this.appToken);

                // Crea la entidad con payload + headers
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

                // Envía la solicitud POST
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        payment.getCallbackURL(),
                        entity,
                        Map.class
                );

                Map<String, Object> body = response.getBody();
                if (body != null && "ACK".equals(body.get("status"))) {
                    String acknowledgeId = (String) body.get("acknowledgeId");
                    payment.setCallbackACKID(acknowledgeId);
                    paymentRepository.save(payment);
                    log.info("ACK recibido. acknowledgeId: " + acknowledgeId);
                    log.info("Response callback: " + body);
                    break; // salir del bucle si todo salió bien
                } else {
                    log.warn("Respuesta inválida, no se recibió ACK en intento #" + attempt);
                }
            } catch (Exception e) {
                log.error("Error al enviar callback en intento #" + attempt + ": " + e.getMessage());
            }

            // Calcular delay según la regla de reintentos
            try {
                if (attempt == 1)
                    Thread.sleep(60_000); // 1 minuto
                else if (attempt == 2)
                    Thread.sleep(120_000); // 2 minutos
                else if (attempt == 3)
                    Thread.sleep(180_000); // 3 minutos
                else
                    Thread.sleep(600_000); // 10 minutos a partir del 4to
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("Reintento interrumpido");
                break;
            }
        }
    }

    // ! Consulta de Pago
    public ResponseEntity<ApiResponse> findPayment(String reference, String paymentId) {

        Optional<Payment> payment = paymentRepository.findByPaymentIdAndReference(Long.valueOf(paymentId), reference);
        log.info("payment encontrado :: " + payment.toString());

        if (payment.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Map<String, Object> data = mapper.convertValue(payment.get(), Map.class);
            return this.buildSuccess(data, "Payment verified successfully");
        }

        return this.buildError(HttpStatus.NOT_FOUND.value(), "Payment not found");
    }

    // ! Listado de Pagos
    public ResponseEntity<ApiResponse> getAllPaymentsPaged(
            String startCreationDate, String endCreationDate,
            String startPaymentDate, String endPaymentDate, String status) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime startCreation = (startCreationDate != null) ? LocalDateTime.parse(startCreationDate, formatter)
                : null;
        LocalDateTime endCreation = (endCreationDate != null) ? LocalDateTime.parse(endCreationDate, formatter) : null;
        LocalDateTime startPayment = (startPaymentDate != null) ? LocalDateTime.parse(startPaymentDate, formatter)
                : null;
        LocalDateTime endPayment = (endPaymentDate != null) ? LocalDateTime.parse(endPaymentDate, formatter) : null;

        List<Payment> payments = paymentRepository.findByCreationDateBetweenAndPaymentDateBetweenOrStatus(
                startCreation, endCreation, startPayment, endPayment, status);

        Map<String, Object> data = new HashMap<>();
        data.put("content", payments);

        return this.buildSuccess(data, "Payments retrieved successfully");
    }

    // ! Cancelación de Pago
    public ResponseEntity<ApiResponse> updatePayment(@Valid PaymentCancelRequest request) {

        Optional<Payment> paymentOpt = paymentRepository.findByReference(request.getReference());
        if (paymentOpt.isEmpty()) {
            return this.buildError(HttpStatus.NOT_FOUND.value(), "Payment not found");
        }

        Payment payment = paymentOpt.get();

        if (!"01".equals(payment.getStatus())) {
            return this.buildError(HttpStatus.CONFLICT.value(), "Payment not canceled");
        }

        payment.setStatus("03");
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", payment.getPaymentId().toString());
        data.put("reference", payment.getReference());
        data.put("cancelDescription", request.getUpdateDescription());
        data.put("creationDate", payment.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("status", payment.getStatus());
        data.put("updatedAt", payment.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("message", "Payment canceled successfully");
        return this.buildSuccess(data, "Payment canceled successfully");
    }

    public ResponseEntity<Map<String, Object>> callbackPayment(PaymentCallBackRequest request) {
        Random random = new Random();
        int acknowledgeId = 10000 + random.nextInt(90000); // 100000-999999

        Map<String, Object> response = new HashMap<>();
        Long paymentId = request.getPaymentId();
        String reference = request.getReference();
        String externalId = request.getExternalId();
        String authorizationNumber = request.getAuthorizationNumber();
        String status = request.getStatus();

        Optional<Payment> payment = paymentRepository
                .findByPaymentIdAndReferenceAndExternalIdAndAuthorizationNumberAndStatus(
                        Long.valueOf(paymentId),
                        reference,
                        externalId,
                        authorizationNumber,
                        status);

        log.info("payment encontrado :: " + payment.toString());

        if (payment.isEmpty()) {
            response.put("acknowledgeId", "");
            response.put("status", "");
            return ResponseEntity.ok(response);
        }

        response.put("acknowledgeId", "ACK-" + acknowledgeId);
        response.put("status", "ACK");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponse> buildSuccess(Map<String, Object> data, String message) {
        ApiResponse response = ApiResponse.builder()
                .response_code(HttpStatus.OK.value())
                .response_message(message)
                .data(data)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<ApiResponse> buildError(int status, String mensaje) {
        ApiResponse response = ApiResponse.builder()
                .response_code(status)
                .response_message(mensaje)
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
