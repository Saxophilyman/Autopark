package org.example.autopark.simpleUser;

import org.example.autopark.service.GeneralDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class SimpleUserValidator implements Validator {
    private final GeneralDetailsService generalDetailsService;

    @Autowired
    public SimpleUserValidator(GeneralDetailsService generalDetailsService) {
        this.generalDetailsService = generalDetailsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return SimpleUser.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        //впоследствии реализовать новый класс(сервис) возвращающий Optional

        SimpleUser simpleUser = (SimpleUser) target;

        try{
            generalDetailsService.loadUserByUsername(simpleUser.getUsername());
        }catch (UsernameNotFoundException ignored){
            return;
        }
        errors.rejectValue("username", "менеджер с таким именем уже существует");
    }
}
