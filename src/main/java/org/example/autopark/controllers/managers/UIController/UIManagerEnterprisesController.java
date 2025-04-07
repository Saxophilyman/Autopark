package org.example.autopark.controllers.managers.UIController;

import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.mapper.EnterpriseMapper;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("managers")
public class UIManagerEnterprisesController {
    private final EnterpriseService enterpriseService;
    private final EnterpriseMapper enterpriseMapper;

    @Autowired
    public UIManagerEnterprisesController(EnterpriseService enterprisesService, EnterpriseMapper enterpriseMapper) {
        this.enterpriseService = enterprisesService;
        this.enterpriseMapper = enterpriseMapper;
    }

    @GetMapping("/enterprises")
    public ModelAndView indexStart(@CurrentManagerId Long managerId) {

        ModelAndView enterprises = new ModelAndView("enterprises/enterprisesStartPage");
        enterprises.addObject("enterprises", enterpriseService.findEnterprisesForManager(managerId));

        return enterprises;
    }

    @GetMapping("/enterprises/{enterpriseId}")
    public String showEnterprise(@CurrentManagerId Long managerId, @PathVariable Long enterpriseId, Model model) {
        // Проверяем, принадлежит ли предприятие текущему менеджеру
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }
        EnterpriseDTO enterpriseDTO = enterpriseMapper.convertToDTO(enterpriseService.findOne(enterpriseId));

        model.addAttribute("enterprise", enterpriseDTO);
        return "enterprises/showEnterprise"; //отображаем страницу просмотра
    }
}
