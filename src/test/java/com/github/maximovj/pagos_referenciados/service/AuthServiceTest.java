package com.github.maximovj.pagos_referenciados.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.github.maximovj.pagos_referenciados.request.AuthRequest;
import com.github.maximovj.pagos_referenciados.response.ApiResponse;
import com.github.maximovj.pagos_referenciados.utils.JwtUtil;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Value("${auth.username}")
    private String username;

    @Value("${auth.password}")
    private String password;

    @Test
    void testGenerateToken_Success() {
        AuthRequest request = new AuthRequest(username, password);

        Mockito.when(jwtUtil.generateToken(username)).thenReturn("mockedToken");

        ResponseEntity<ApiResponse> response = authService.generateToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Authentication successful", response.getBody().getResponse_message());
        assertTrue(((Map<String, Object>)response.getBody().getData()).containsKey("token"));
    }

    @Test
    void testGenerateToken_Failure() {
        AuthRequest request = new AuthRequest("wrongUser", "wrongPass");

        ResponseEntity<ApiResponse> response = authService.generateToken(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Authentication failed", response.getBody().getResponse_message());
        assertNull(response.getBody().getData());
    }
}