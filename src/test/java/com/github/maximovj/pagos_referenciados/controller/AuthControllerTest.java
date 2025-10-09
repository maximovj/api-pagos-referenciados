package com.github.maximovj.pagos_referenciados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximovj.pagos_referenciados.request.AuthRequest;
import com.github.maximovj.pagos_referenciados.response.ApiResponse;
import com.github.maximovj.pagos_referenciados.service.AuthService;
import com.github.maximovj.pagos_referenciados.utils.JwtUtil;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // ⚠️ Desactiva filtros de seguridad
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil; // ✅ Mock para evitar error de JwtFilter

    @Test
    void testAuthenticate_Success() throws Exception {
        // Mock de respuesta exitosa
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mockedToken");
        data.put("createdAt", "2025-10-08 18:00:00");

        ApiResponse apiResponse = ApiResponse.builder()
                .response_code(HttpStatus.OK.value())
                .response_message("Authentication successful")
                .data(data)
                .build();

        Mockito.when(authService.generateToken(Mockito.any(AuthRequest.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        AuthRequest req = new AuthRequest();
        req.setUsername("usuario_test");
        req.setPassword("password_secreto");

        // Realizar petición POST al endpoint
        mockMvc.perform(post("/api/v1/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response_message").value("Authentication successful"))
                .andExpect(jsonPath("$.data.token").value("mockedToken"));
    }

    @Test
    void testAuthenticate_Failure() throws Exception {
        // Mock de respuesta fallida
        ApiResponse apiResponse = ApiResponse.builder()
                .response_code(HttpStatus.BAD_REQUEST.value())
                .response_message("Authentication failed")
                .data(null)
                .build();

        Mockito.when(authService.generateToken(Mockito.any(AuthRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse));
        
        AuthRequest req = new AuthRequest();
        req.setUsername("");
        req.setPassword("");

        mockMvc.perform(post("/api/v1/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response_code").value(400))
                .andExpect(jsonPath("$.response_message").value("Authentication failed"));
    }
}