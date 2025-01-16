//package org.example.autopark.controllers;
//
//import jakarta.validation.Valid;
//import org.example.autopark.entity.Manager;
//import org.example.autopark.security.JWTUtil;
//import org.example.autopark.service.RegistrationService;
//import org.example.autopark.util.ManagerValidator;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.web.csrf.CsrfToken;
//import org.springframework.stereotype.Controller;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/auth")
//public class AuthController {
//
//    private final ManagerValidator managerValidator;
//    private final RegistrationService registrationService;
//    private final JWTUtil jwtUtil;
//    private final ModelMapper modelMapper;
//
//    @Autowired
//    public AuthController(ManagerValidator managerValidator, RegistrationService registrationService, JWTUtil jwtUtil, ModelMapper modelMapper) {
//        this.managerValidator = managerValidator;
//        this.registrationService = registrationService;
//        this.jwtUtil = jwtUtil;
//        this.modelMapper = modelMapper;
//    }
//
//    @GetMapping("/login")
//    public String loginPage() {
//        return "auth/login";
//    }
//
////    @GetMapping("/start")
////    public String startPage() {
////        return "start";
////    }
//
//    @GetMapping("/registration")
//    public String registrationPage(@ModelAttribute("manager") Manager manager) {
//        return "auth/registration";
//    }
//
//
//    @PostMapping("/registration")
//    public String performRegistration(@ModelAttribute("manager") @Valid Manager manager,
//                                      BindingResult bindingResult) {
//        if (bindingResult.hasErrors())
//            return "auth/registration";
//
//        managerValidator.validate(manager, bindingResult);
//        registrationService.register(manager);
//        return "redirect:/auth/login";
//    }
//
//
//}
