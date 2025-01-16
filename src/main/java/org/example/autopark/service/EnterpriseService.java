package org.example.autopark.service;

import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Manager;
import org.example.autopark.exception.EnterpriseNotDeletedException;
import org.example.autopark.exception.EnterpriseNotUpdatedException;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
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

    public List<Enterprise> findEnterprisesForManager(Long id) {
        return enterpriseRepository.findEnterprisesByManagerList_managerId(id);
    }
//ищет все предприятия для пользователя
//    public List<Enterprise> findEnterprisesForUser() {
//        return enterpriseRepository.findEnterprises();
//    }

    @Transactional
    public void save(Enterprise enterprise, Long id) {
        Optional<Manager> currentManager = managersRepository.findById(id);

        if (enterprise.getManagerList() == null) {
            enterprise.setManagerList(new ArrayList<>());
        }

        enterprise.getManagerList().add(currentManager.get());
        enterpriseRepository.save(enterprise);
    }


    @Transactional
    public void update(Long idManager, Long idEnterprise, Enterprise updatedEnterprise) {
        Enterprise enterprise = enterpriseRepository.findById(idEnterprise).get();
        List<Enterprise> managerEnterprises = managersRepository.findById(idManager).get().getEnterpriseList();
        if(!managerEnterprises.contains(enterprise)) {
            throw new EnterpriseNotUpdatedException("Нет доступа к данному предприятию");
        }
        List<Manager> managers = enterpriseRepository.findById(idEnterprise).get().getManagerList();

        updatedEnterprise.setManagerList(managers);
        updatedEnterprise.setEnterpriseId(idEnterprise);

        enterpriseRepository.save(updatedEnterprise);
    }

    public void delete(Long idManager, Long idEnterprise) {
        Enterprise enterprise = enterpriseRepository.findById(idEnterprise).get();
        List<Enterprise> managerEnterprises = managersRepository.findById(idManager).get().getEnterpriseList();

        if(!managerEnterprises.contains(enterprise)) {
            throw new EnterpriseNotDeletedException("Нет доступа к данному предприятию");
        }

        // Удаление предприятия из списка у всех менеджеров, у которых оно есть
        for (Manager manager : enterprise.getManagerList()) {
            manager.getEnterpriseList().remove(enterprise);
        }

        enterpriseRepository.deleteById(idEnterprise);
    }
}
