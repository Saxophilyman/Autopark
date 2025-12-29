package org.example.autopark.util;

import org.example.autopark.entity.Manager;
import org.example.autopark.service.GeneralDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@Profile("!reactive")
public class ManagerValidator implements Validator {
    private final GeneralDetailsService generalDetailsService;

    @Autowired
    public ManagerValidator(GeneralDetailsService generalDetailsService) {
        this.generalDetailsService = generalDetailsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Manager.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        //впоследствии реализовать новый класс(сервис) возвращающий Optional

        Manager manager = (Manager) target;

        try{
            generalDetailsService.loadUserByUsername(manager.getUsername());
        }catch (UsernameNotFoundException ignored){
            return;
        }
        errors.rejectValue("username", "менеджер с таким именем уже существует");
    }
}
