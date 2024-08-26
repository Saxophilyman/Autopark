package org.example.autopark.repository;

import org.example.autopark.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    List<Enterprise> findEnterprisesByManagerList_managerId(Long id);
}
