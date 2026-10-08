package com.luv2code.ecommerce.controller;

import com.luv2code.ecommerce.dto.AuthResponse;
import com.luv2code.ecommerce.dto.LoginRequest;
import com.luv2code.ecommerce.dto.RegisterRequest;
import com.luv2code.ecommerce.dto.UserDto;
import com.luv2code.ecommerce.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(Principal principal) {
        return authService.me(principal.getName());
    }
}
