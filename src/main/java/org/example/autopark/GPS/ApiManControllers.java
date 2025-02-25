package org.example.autopark.GPS;

import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/managers")
public class ApiManControllers {
    private final PointsGPSService pointsGPSService;

    @Autowired
    public ApiManControllers(PointsGPSService pointsGPSService){
        this.pointsGPSService = pointsGPSService;
    }

    @GetMapping("/points")
    public List<String> indexPointsGPS(@CurrentManagerId Long id) {
        return pointsGPSService.findAll();
    }

}
