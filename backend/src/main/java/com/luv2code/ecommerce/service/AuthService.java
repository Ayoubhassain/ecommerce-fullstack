package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.dao.UserRepository;
import com.luv2code.ecommerce.dto.AuthResponse;
import com.luv2code.ecommerce.dto.LoginRequest;
import com.luv2code.ecommerce.dto.RegisterRequest;
import com.luv2code.ecommerce.dto.UserDto;
import com.luv2code.ecommerce.entity.Role;
import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.exception.ConflictException;
import com.luv2code.ecommerce.exception.NotFoundException;
import com.luv2code.ecommerce.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account already exists with this email");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setRole(Role.USER); // public sign-up never creates an admin
        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user), UserDto.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                // same message whether the email or the password is wrong
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        return new AuthResponse(jwtService.generateToken(user), UserDto.from(user));
    }

    public UserDto me(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserDto::from)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
