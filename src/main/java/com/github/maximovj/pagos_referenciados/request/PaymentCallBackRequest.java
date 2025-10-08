package com.github.maximovj.pagos_referenciados.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCallBackRequest {

    @NotNull(message = "El campo 'paymentId' es obligatorio")
    private Long paymentId;

    @NotBlank(message = "El campo 'externalId' es obligatorio")
    private String externalId;

    @NotNull(message = "El campo 'amount' es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private Double amount;

    @NotBlank(message = "El campo 'authorizationNumber' es obligatorio")
    private String authorizationNumber;

    @NotBlank(message = "El campo 'reference' es obligatorio")
    @Size(min = 30, max = 50, message = "La referencia debe tener entre 30 y 50 caracteres")
    private String reference;

    @NotBlank(message = "El campo 'paymentDate' es obligatorio")
    private String paymentDate; // formato esperado: "yyyy-MM-dd HH:mm:ss"

    @NotBlank(message = "El campo 'status' es obligatorio")
    @Pattern(regexp = "01|02|03|04|05", message = "El estado debe ser uno de los siguientes valores: 01, 02, 03, 04, 05")
    private String status;

    private String message;
}
