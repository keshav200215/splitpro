package com.splitwise.controller;

import com.splitwise.entity.User;
import com.splitwise.repository.UserRepository;
import com.splitwise.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.splitwise.security.JwtUtil;
import java.util.*;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
public String signup(@RequestBody SignupRequest request) {

    Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

    if (existingUser.isPresent()) {

        User user = existingUser.get();

        if (user.getInvited()) {

            user.setName(request.getName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setInvited(false);

            userRepository.save(user);

            return "Signup successful";
        }

        throw new RuntimeException("Email already registered");
    }

    User user = userRepository.save(
            User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .invited(false)
                    .build()
    );

    return "Signup successful";
}
    @PostMapping("/login")
public String login(@RequestBody User user) {

    var existingUser = userRepository.findByEmail(user.getEmail());

    if (existingUser.isEmpty()) {
        return "User not found!";
    }

    if (!passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
        return "Invalid password!";
    }

    return jwtUtil.generateToken(existingUser.get().getEmail());
}
}