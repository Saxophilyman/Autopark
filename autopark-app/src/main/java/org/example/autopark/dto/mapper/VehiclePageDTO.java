package org.example.autopark.dto.mapper;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.autopark.dto.VehicleApiDto;
import org.example.autopark.dto.VehicleDTO;

import java.util.List;

@Data
@AllArgsConstructor
public class VehiclePageDTO {
    private List<VehicleApiDto> vehicles;
    private int currentPage;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private String enterpriseTimezone;
}
