package com.github.maximovj.pagos_referenciados.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCancelRequest {

    @NotBlank(message = "La descripción de actualización es obligatoria")
    private String updateDescription;

    @Pattern(regexp = "03", message = "El status debe ser 03")
    private String status;

    @Size(min = 30, max = 30, message = "La referencia debe tener exactamente 30 caracteres")
    private String reference;

}
