package com.github.maximovj.pagos_referenciados.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.maximovj.pagos_referenciados.model.Payment;

public interface IPaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByReference(String reference);

    Optional<Payment> findByPaymentIdAndReference(Long id, String reference);

    List<Payment> findByCreationDateBetweenAndPaymentDateBetweenOrStatus(
            LocalDateTime startCreation, LocalDateTime endCreation,
            LocalDateTime startPayment, LocalDateTime endPayment,
            String status);

    Optional<Payment> findByReference(String reference);

    Optional<Payment> findByPaymentIdAndReferenceAndExternalIdAndAuthorizationNumberAndStatus(Long id, String reference,
            String externalId, String authorizationNumber, String status);

}
