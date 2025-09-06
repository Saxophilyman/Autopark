package org.example.autopark.controllers.managers.APIControllers;

import lombok.extern.slf4j.Slf4j;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.mapper.EnterpriseMapper;
import org.example.autopark.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
public class ApiManagerEnterprisesController {
    private final EnterpriseService enterprisesService;
    private final EnterpriseMapper enterpriseMapper;

    @Autowired
    public ApiManagerEnterprisesController(EnterpriseService enterprisesService, EnterpriseMapper enterpriseMapper) {
        this.enterprisesService = enterprisesService;
        this.enterpriseMapper = enterpriseMapper;
    }

    @GetMapping("/enterprises")
    public List<EnterpriseDTO> indexEnterprises(@CurrentManagerId Long managerId) {
        return enterprisesService.findEnterprisesForManager(managerId)
                .stream().map(enterpriseMapper::convertToDTO).collect(Collectors.toList());
    }

}
