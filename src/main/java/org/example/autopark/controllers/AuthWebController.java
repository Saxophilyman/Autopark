package org.example.autopark.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Profile("!reactive")
@RequestMapping("/auth")
public class AuthWebController {


}
