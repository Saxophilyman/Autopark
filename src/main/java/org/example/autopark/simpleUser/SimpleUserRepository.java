package org.example.autopark.simpleUser;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimpleUserRepository extends JpaRepository<SimpleUser, Integer> {
    Optional<SimpleUser> findByUsername(String username);
}
