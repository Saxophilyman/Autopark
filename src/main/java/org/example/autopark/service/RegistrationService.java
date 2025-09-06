package org.example.autopark.service;

import org.example.autopark.entity.Manager;
import org.example.autopark.repository.ManagerRepository;
import org.example.autopark.simpleUser.SimpleUser;
import org.example.autopark.simpleUser.SimpleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!reactive")
public class RegistrationService {
    //private final ManagerRepository managerRepository;
    private final SimpleUserRepository simpleUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(SimpleUserRepository simpleUserRepository, PasswordEncoder passwordEncoder) {
        this.simpleUserRepository = simpleUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(SimpleUser simpleUser) {

        simpleUser.setPassword(passwordEncoder.encode(simpleUser.getPassword()));
        simpleUserRepository.save(simpleUser);
    }
}
