package org.example.autopark.simpleUser;

import org.example.autopark.dto.DriverDTO;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.security.ManagerDetails;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
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




    @GetMapping("{id}")
    public String helloUSer(@PathVariable int id) {
        return "Hello, user " + id;
    }

    @GetMapping
    public ModelAndView start(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SimpleUserDetails simpleUserDetails = (SimpleUserDetails) authentication.getPrincipal();

        //System.out.println(managerDetails.getManager().getUsername());

        model.addAttribute("simpleUser", simpleUserDetails.getSimpleUser());

        return new ModelAndView("userStartPage");
    }

//    @GetMapping("/{id}/enterprises")
//    public List<Enterprise> indexEnterprises(@PathVariable("id") Long id) {
//        return enterprisesService.findEnterprisesForUser();
//    }

//    @GetMapping("/{id}/vehicles")
//    public List<VehicleDTO> indexVehicles(@PathVariable("id") Long id) {
//        return vehiclesService.findVehiclesForManager(id).stream().map(this::convertToVehicleDTO)
//                .collect(Collectors.toList());
//    }
//
    private VehicleDTO convertToVehicleDTO(Vehicle vehicle) {
        return modelMapper.map(vehicle, VehicleDTO.class);
    }

//    @ResponseBody
//    @GetMapping("/{id}/drivers")
//    public List<DriverDTO> indexDrivers(@PathVariable("id") Long id) {
//        return driversService.findAll().stream().map(this::convertToDriverDTO)
//                .collect(Collectors.toList());
//    }
//
    private DriverDTO convertToDriverDTO(Driver driver) {
        return modelMapper.map(driver, DriverDTO.class);
    }
}
