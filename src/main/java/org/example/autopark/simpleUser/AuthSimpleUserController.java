package org.example.autopark.simpleUser;

import jakarta.validation.Valid;
import org.example.autopark.entity.Manager;
import org.example.autopark.security.JWTUtil;
import org.example.autopark.service.RegistrationService;
import org.example.autopark.util.ManagerValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthSimpleUserController {

    private final SimpleUserValidator simpleUserValidator;
    private final RegistrationService registrationService;

    @Autowired
    public AuthSimpleUserController(SimpleUserValidator simpleUserValidator, RegistrationService registrationService) {
        this.simpleUserValidator = simpleUserValidator;
        this.registrationService = registrationService;
    }
//    private final JWTUtil jwtUtil;
//    private final ModelMapper modelMapper;


    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

//    @GetMapping("/start")
//    public String startPage() {
//        return "start";
//    }

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("simpleUser") SimpleUser simpleUser) {
        return "auth/registrationSimpleUser";
    }


    @PostMapping("/registration")
    public String performRegistration(@ModelAttribute("simpleUser") @Valid SimpleUser simpleUser,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "auth/registration";

        simpleUserValidator.validate(simpleUser, bindingResult);
        registrationService.register(simpleUser);
        return "redirect:/auth/login";
    }


}
