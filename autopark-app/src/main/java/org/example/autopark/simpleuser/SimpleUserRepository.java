package org.example.autopark.simpleuser;


import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("!reactive")
public interface SimpleUserRepository extends JpaRepository<SimpleUser, Integer> {
    Optional<SimpleUser> findByUsername(String username);
}
