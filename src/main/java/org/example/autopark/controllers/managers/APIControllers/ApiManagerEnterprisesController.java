package org.example.autopark.controllers.managers.APIControllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.EnterpriseDTO;

import org.example.autopark.dto.mapper.EnterpriseMapper;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.security.SecurityUtil;
import org.example.autopark.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
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

//    @PutMapping("/{id}/enterprises/{idEnterprise}")
//    public ResponseEntity<HttpStatus> update(@PathVariable("id") Long idManager,
//                                             @RequestBody @Valid Enterprise enterprise,
//                                             BindingResult bindingResult,
//                                             @PathVariable("idEnterprise") Long idEnterprise) {
//        ValidationBindingUtil.Binding(bindingResult);
//        enterprisesService.update(idManager, idEnterprise, enterprise);
//        return ResponseEntity.ok(HttpStatus.OK);
//    }
}
