package org.example.autopark.datageneration.application;

import org.example.autopark.datageneration.contract.EnterpriseDataStore;
import org.example.autopark.datageneration.contract.GenerationCatalog;
import org.example.autopark.datageneration.generation.DriverGenerator;
import org.example.autopark.datageneration.generation.EveryNVehicleAssignmentPolicy;
import org.example.autopark.datageneration.generation.VehicleGenerator;
import org.example.autopark.datageneration.model.DriverDraft;
import org.example.autopark.datageneration.model.DriverVehicleAssignment;
import org.example.autopark.datageneration.model.GeneratedEnterpriseData;
import org.example.autopark.datageneration.model.GenerationContext;
import org.example.autopark.datageneration.model.VehicleDraft;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Profile("!reactive")
@Transactional(readOnly = true)
public class GenerateEnterpriseDataService implements GenerateEnterpriseDataUseCase {

    private final GenerationCatalog generationCatalog;
    private final VehicleGenerator vehicleGenerator;
    private final DriverGenerator driverGenerator;
    private final EveryNVehicleAssignmentPolicy assignmentPolicy;
    private final EnterpriseDataStore enterpriseDataStore;

    public GenerateEnterpriseDataService(
            GenerationCatalog generationCatalog,
            VehicleGenerator vehicleGenerator,
            DriverGenerator driverGenerator,
            EveryNVehicleAssignmentPolicy assignmentPolicy,
            EnterpriseDataStore enterpriseDataStore
    ) {
        this.generationCatalog = generationCatalog;
        this.vehicleGenerator = vehicleGenerator;
        this.driverGenerator = driverGenerator;
        this.assignmentPolicy = assignmentPolicy;
        this.enterpriseDataStore = enterpriseDataStore;
    }

    @Override
    @Transactional
    public void execute(GenerateEnterpriseDataCommand command) {
        Set<String> licensePlatesGeneratedInThisRequest = new HashSet<>();

        for (Long enterpriseId : command.enterpriseIds()) {
            GenerationContext context = generationCatalog.load(enterpriseId);

            Set<String> occupiedLicensePlates = new HashSet<>(context.occupiedLicensePlates());
            occupiedLicensePlates.addAll(licensePlatesGeneratedInThisRequest);

            List<VehicleDraft> vehicles = vehicleGenerator.generate(
                    context,
                    command.vehicleCount(),
                    occupiedLicensePlates
            );

            List<DriverDraft> drivers = driverGenerator.generate(command.driverCount());

            List<DriverVehicleAssignment> assignments = assignmentPolicy.assign(
                    vehicles.size(),
                    drivers.size(),
                    command.assignmentStep()
            );

            GeneratedEnterpriseData generatedData = new GeneratedEnterpriseData(
                    context.enterpriseId(),
                    vehicles,
                    drivers,
                    assignments
            );

            enterpriseDataStore.save(generatedData);
            vehicles.stream()
                    .map(VehicleDraft::licensePlate)
                    .forEach(licensePlatesGeneratedInThisRequest::add);
        }
    }
}
