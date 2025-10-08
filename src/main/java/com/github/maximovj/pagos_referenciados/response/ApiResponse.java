package com.github.maximovj.pagos_referenciados.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse {

    private int response_code;
    private String response_message;
    private Map<String, Object> data;

}
