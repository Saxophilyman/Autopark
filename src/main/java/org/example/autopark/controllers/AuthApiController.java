package org.example.autopark.controllers;

import jakarta.validation.Valid;


import org.example.autopark.securityConfig.jwt.JwtUtil;
import org.example.autopark.service.GeneralDetailsService;
import org.example.autopark.service.RegistrationService;

import org.example.autopark.simpleUser.SimpleUser;
import org.example.autopark.simpleUser.SimpleUserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@Profile("!reactive")
@RequestMapping("/auth")
public class AuthApiController {

    private final SimpleUserValidator simpleUserValidator;
    private final RegistrationService registrationService; //куда это девается?
    private final JwtUtil jwtUtil;
    private final GeneralDetailsService generalDetailsService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthApiController(SimpleUserValidator simpleUserValidator,
                             RegistrationService registrationService,
                             JwtUtil jwtUtil, GeneralDetailsService generalDetailsService, AuthenticationManager authenticationManager) {
        this.simpleUserValidator = simpleUserValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.generalDetailsService = generalDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = generalDetailsService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/login2")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = generalDetailsService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }



    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("simpleUser") SimpleUser simpleUser) {
        return "auth/registrationSimpleUser";
    }

    @PostMapping("/registration")
    public String performRegistration(@ModelAttribute("simpleUser") @Valid SimpleUser simpleUser,
                                      BindingResult bindingResult) {
        simpleUserValidator.validate(simpleUser, bindingResult);
        if (bindingResult.hasErrors())
            return "auth/registration";
        registrationService.register(simpleUser);
        return "redirect:/auth/login";
    }
}
