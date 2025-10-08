package com.github.maximovj.pagos_referenciados.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.github.maximovj.pagos_referenciados.request.AuthRequest;
import com.github.maximovj.pagos_referenciados.response.ApiResponse;
import com.github.maximovj.pagos_referenciados.utils.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private JwtUtil jwt;

    // Usuario y password compartido con el implementador
    @Value("${auth.username}")
    private String USERNAME;

    @Value("${auth.password}")
    private String PASSWORD;

    public ResponseEntity<ApiResponse> generateToken(AuthRequest request) {

        if (!USERNAME.equals(request.getUsername()) || !PASSWORD.equals(request.getPassword())) {
            ApiResponse response = ApiResponse.builder()
                    .response_code(HttpStatus.BAD_REQUEST.value())
                    .response_message("Authentication failed")
                    .data(null)
                    .build();

            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = jwt.generateToken(request.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Crear respuesta (response)
        ApiResponse response = ApiResponse.builder()
                .response_code(HttpStatus.OK.value())
                .response_message("Authentication successful")
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
