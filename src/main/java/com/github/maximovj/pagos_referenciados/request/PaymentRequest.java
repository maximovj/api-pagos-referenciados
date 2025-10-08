package com.github.maximovj.pagos_referenciados.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private Double amount;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotBlank(message = "La fecha de vencimiento es obligatoria")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", message = "Formato de fecha inválido: yyyy-MM-dd HH:mm:ss")
    private String dueDate;

    private String callbackURL;

    private String externalId;
}