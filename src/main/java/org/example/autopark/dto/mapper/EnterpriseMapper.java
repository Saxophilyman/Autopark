package org.example.autopark.dto.mapper;

import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.entity.Enterprise;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EnterpriseMapper {
    private final ModelMapper modelMapper;

    public EnterpriseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public EnterpriseDTO convertToDTO(Enterprise enterprise) {
        EnterpriseDTO dto = modelMapper.map(enterprise, EnterpriseDTO.class);
        return dto; // Просто маппим без изменений, время уже есть в сущности
    }

    public Enterprise convertToEntity(EnterpriseDTO dto) {
        Enterprise enterprise = modelMapper.map(dto, Enterprise.class);

        // Если таймзона не задана или пустая, ставим UTC
        if (dto.getTimeZone() == null || dto.getTimeZone().isBlank()) {
            enterprise.setTimeZone("UTC");
        }

        return enterprise;
    }
}
