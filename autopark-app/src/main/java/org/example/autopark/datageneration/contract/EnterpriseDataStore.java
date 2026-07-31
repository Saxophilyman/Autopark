package org.example.autopark.datageneration.contract;

import org.example.autopark.datageneration.model.GeneratedEnterpriseData;

/**
 * Интерфейс сохранения подготовленных данных предприятия.
 */
public interface EnterpriseDataStore {

    void save(GeneratedEnterpriseData data);
}
