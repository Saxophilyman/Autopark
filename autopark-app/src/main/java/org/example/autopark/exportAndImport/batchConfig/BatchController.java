package org.example.autopark.exportAndImport.batchConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("!reactive")
@Slf4j
public class BatchController {

    private final Job importVehicleGuidJob;
    private final Job importVehicleByIdJob;
    private final JobLauncher jobLauncher;

    public BatchController(@Qualifier("importVehicleByGuidJob") Job importVehicleGuidJob,
                           @Qualifier("importVehicleByIdJob") Job importVehicleByIdJob,
                           JobLauncher jobLauncher) {
        this.importVehicleGuidJob = importVehicleGuidJob;
        this.importVehicleByIdJob = importVehicleByIdJob;
        this.jobLauncher = jobLauncher;
    }

    @PostMapping("/api/managers/import-batch")
    public String startBatchImport() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(importVehicleByIdJob, parameters);
            return "Импорт через Spring Batch (по ID) запущен!";
        } catch (Exception e) {
            log.error("Ошибка запуска batch импорта по ID", e);
            return "Ошибка запуска: " + e.getMessage();
        }
    }

    @PostMapping("/api/managers/import-batch-guid")
    public String startGuidImport() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(importVehicleGuidJob, parameters);
            return "Импорт по GUID через Spring Batch запущен!";
        } catch (Exception e) {
            log.error("Ошибка запуска batch импорта по GUID", e);
            return "Ошибка запуска: " + e.getMessage();
        }
    }
}
