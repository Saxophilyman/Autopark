package org.example.autopark.exportAndImport.ref.entry.batch;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("!reactive")
@Hidden
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
    public ResponseEntity<String> startBatchImport() {
        launch(importVehicleByIdJob, "Не удалось запустить batch-импорт по ID");
        return ResponseEntity.accepted().body("Импорт через Spring Batch по ID запущен");
    }

    @PostMapping("/api/managers/import-batch-guid")
    public ResponseEntity<String> startGuidImport() {
        launch(importVehicleGuidJob, "Не удалось запустить batch-импорт по GUID");
        return ResponseEntity.accepted().body("Импорт через Spring Batch по GUID запущен");
    }

    private void launch(Job job, String errorMessage) {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, parameters);
        } catch (Exception exception) {
            throw new BatchLaunchException(errorMessage, exception);
        }
    }
}
