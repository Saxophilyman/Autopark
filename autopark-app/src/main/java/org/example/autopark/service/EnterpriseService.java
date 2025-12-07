package org.example.autopark.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Manager;
import org.example.autopark.exception.EnterpriseNotDeletedException;
import org.example.autopark.exception.EnterpriseNotUpdatedException;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Profile("!reactive")
public class EnterpriseService {
    private final EnterpriseRepository enterpriseRepository;
    private final ManagerRepository managersRepository;

    @Autowired
    public EnterpriseService(EnterpriseRepository enterprisesRepository, ManagerRepository managersRepository) {
        this.enterpriseRepository = enterprisesRepository;
        this.managersRepository = managersRepository;
    }

    public List<Enterprise> findAll() {
        return enterpriseRepository.findAll();
    }

    public Enterprise findOne(Long id) {
        Optional<Enterprise> foundEnterprise = enterpriseRepository.findById(id);
        return foundEnterprise.orElse(null);
    }

    //безопасное получение предприятия с учётом менеджера.
    public Enterprise findEnterpriseForManager(Long managerId, Long enterpriseId) {
        if (!managerHasEnterprise(managerId, enterpriseId)) {
            throw new EntityNotFoundException("Предприятие не найдено или недоступно для данного менеджера");
        }
        return getEnterpriseOrThrow(enterpriseId);
    }

    public List<Enterprise> findEnterprisesForManager(Long id) {
        return enterpriseRepository.findEnterprisesByManagerList_managerId(id);
    }

    @Transactional
    public void save(Enterprise enterprise, Long managerId) {
        Manager manager = managersRepository.findById(managerId)
                .orElseThrow(() -> new EntityNotFoundException("Менеджер не найден"));

        if (enterprise.getManagerList() == null) {
            enterprise.setManagerList(new ArrayList<>());
        }

        enterprise.getManagerList().add(manager);
        enterpriseRepository.save(enterprise);
    }


    @Transactional
    public void update(Long managerId, Long enterpriseId, Enterprise updatedEnterprise) {
        // 1. Проверяем доступ менеджера
        if (!managerHasEnterprise(managerId, enterpriseId)) {
            throw new EnterpriseNotUpdatedException("Нет доступа к данному предприятию");
        }

        // 2. Грузим существующее предприятие (одно обращение к БД)
        Enterprise existing = getEnterpriseOrThrow(enterpriseId);

        // 3. Сохраняем связь с менеджерами
        updatedEnterprise.setEnterpriseId(enterpriseId);
        updatedEnterprise.setManagerList(existing.getManagerList());

        enterpriseRepository.save(updatedEnterprise);
    }

    @Transactional
    public void delete(Long managerId, Long enterpriseId) {
        if (!managerHasEnterprise(managerId, enterpriseId)) {
            throw new EnterpriseNotDeletedException("Нет доступа к данному предприятию");
        }

        Enterprise enterprise = getEnterpriseOrThrow(enterpriseId);

        // Удаляем связи с менеджерами
        for (Manager manager : enterprise.getManagerList()) {
            manager.getEnterpriseList().remove(enterprise);
        }

        enterpriseRepository.delete(enterprise);
    }

    public boolean managerHasEnterprise(Long managerId, Long enterpriseId) {
        Manager manager = getManagerOrThrow(managerId);
        return enterpriseRepository.existsByManagerListContainsAndEnterpriseId(manager, enterpriseId);
    }

    private Manager getManagerOrThrow(Long managerId) {
        return managersRepository.findById(managerId)
                .orElseThrow(() -> new EntityNotFoundException("Менеджер не найден"));
    }
    private Enterprise getEnterpriseOrThrow(Long enterpriseId) {
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new EntityNotFoundException("Предприятие не найдено"));
    }
}
