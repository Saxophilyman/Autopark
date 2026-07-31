package org.example.autopark.datageneration.application;

/**
 * Прикладной сценарий наполнения предприятий автомобилями и водителями.
 */
public interface GenerateEnterpriseDataUseCase {

    void execute(GenerateEnterpriseDataCommand command);
}
