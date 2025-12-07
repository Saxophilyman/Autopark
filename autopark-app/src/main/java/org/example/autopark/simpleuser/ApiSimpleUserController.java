package org.example.autopark.simpleuser;

import io.swagger.v3.oas.annotations.Hidden;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Hidden
@Controller
@Profile("!reactive")
@RequestMapping("api/users")
public class ApiSimpleUserController {
    private final EnterpriseService enterprisesService;
    private final VehicleService vehiclesService;
    private final DriverService driversService;
    private final ModelMapper modelMapper;

    @Autowired
    public ApiSimpleUserController(EnterpriseService enterprisesService,
                                   VehicleService vehiclesService,
                                   DriverService driversService,
                                   ModelMapper modelMapper) {
        this.enterprisesService = enterprisesService;
        this.vehiclesService = vehiclesService;
        this.driversService = driversService;
        this.modelMapper = modelMapper;
    }


//    Пока что просто скрываю весь контроллер @Hidden
//    @Operation(
//            summary = "Тестовый эндпоинт для простого пользователя",
//            description = "Возвращает простой текст Hello, user! если авторизация прошла."
//    )
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello, user!");
    }

    @GetMapping
    public ModelAndView start(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SimpleUserDetails simpleUserDetails = (SimpleUserDetails) authentication.getPrincipal();

        //System.out.println(managerDetails.getManager().getUsername());

        model.addAttribute("simpleUser", simpleUserDetails.getSimpleUser());

        return new ModelAndView("userStartPage");
    }


}
