package org.example.autopark.service;

import org.example.autopark.entity.Manager;
import org.example.autopark.repository.ManagerRepository;
import org.example.autopark.security.ManagerDetails;
import org.example.autopark.simpleuser.SimpleUser;
import org.example.autopark.simpleuser.SimpleUserDetails;
import org.example.autopark.simpleuser.SimpleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Profile("!reactive")
public class GeneralDetailsService implements UserDetailsService {
    private final ManagerRepository managerRepository;
    private final SimpleUserRepository simpleUserRepository;

    @Autowired
    public GeneralDetailsService(ManagerRepository managerRepository,
                                 SimpleUserRepository simpleUserRepository) {
        this.managerRepository = managerRepository;
        this.simpleUserRepository = simpleUserRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Сначала ищем среди менеджеров
    Optional<Manager> manager = managerRepository.findByUsername(username);
        if (manager.isPresent()) {
        return new ManagerDetails(manager.get());
    }

    // Затем ищем среди обычных пользователей
    Optional<SimpleUser> user = simpleUserRepository.findByUsername(username);
        if (user.isPresent()) {
        return new SimpleUserDetails(user.get());
    }

        throw new UsernameNotFoundException("User not found with username: " + username);
}
}
