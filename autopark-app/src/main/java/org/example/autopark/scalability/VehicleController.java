package org.example.autopark.scalability;

import lombok.RequiredArgsConstructor;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rps/api/vehicles")
public class VehicleController {
    private final VehicleService service;
    private final ModelMapper modelMapper;

    @io.micrometer.core.annotation.Timed(
            value = "vehicles.getById",
            histogram = true
            // percentiles можно не указывать — Prometheus берёт перцентили из гистограмм через histogram_quantile
    )
    @GetMapping("/{id}")
    public VehicleDTO get(@PathVariable Long id) {
        return convertToVehicleDTO(service.findOne(id));
    }

    

    private VehicleDTO convertToVehicleDTO(Vehicle vehicle) {
        return modelMapper.map(vehicle, VehicleDTO.class);
    }
}
