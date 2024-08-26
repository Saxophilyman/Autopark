package org.example.autopark.service;

import org.example.autopark.entity.Manager;
import org.example.autopark.repository.ManagerRepository;
import org.example.autopark.security.ManagerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ManagerDetailsService implements UserDetailsService {
    private final ManagerRepository managerRepository;

    @Autowired
    public ManagerDetailsService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Manager> manager = managerRepository.findByUsername(username);
        if (manager.isEmpty()) {
            throw new UsernameNotFoundException("USer not found");
        }
        return new ManagerDetails(manager.get());
    }
}
