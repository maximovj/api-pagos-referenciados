package com.github.maximovj.pagos_referenciados.model;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(unique = true, nullable = false)
    private String reference;

    private Double amount;

    private String description;

    private String status;

    @Column(unique = true, nullable = false)
    private String externalId;

    private String callbackURL;

    @Column(unique = true, nullable = false)
    private String callbackACKID;

    private String authorizationNumber;

    private String dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.paymentDate = LocalDateTime.now();

        String reference = "PRV" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 27)
                .toUpperCase();
        this.reference = reference;

        Random random = new Random();
        int authorizationNumber = 100000 + random.nextInt(900000); // 100000-999999
        this.authorizationNumber = String.valueOf(authorizationNumber);

        // int externalId = 10000 + random.nextInt(90000); // 100000-999999
        // this.externalId = "ORD-" + externalId;
    }
}