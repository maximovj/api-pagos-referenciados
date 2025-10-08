package com.github.maximovj.pagos_referenciados.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.maximovj.pagos_referenciados.request.AuthRequest;
import com.github.maximovj.pagos_referenciados.response.ApiResponse;
import com.github.maximovj.pagos_referenciados.service.AuthService;

@RestController
@RequestMapping("/api/v1")
@Validated
public class AuthController {

    @Autowired
    private AuthService service;

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse> authenticate(
            @RequestBody AuthRequest request) {
        return service.generateToken(request);
    }

}
