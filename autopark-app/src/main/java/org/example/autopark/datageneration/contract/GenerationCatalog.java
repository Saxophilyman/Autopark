package org.example.autopark.datageneration.contract;

import org.example.autopark.datageneration.model.GenerationContext;

/**
 * Интерфейс получения исходных данных, необходимых для генерации.
 */
public interface GenerationCatalog {

    GenerationContext load(Long enterpriseId);
}
