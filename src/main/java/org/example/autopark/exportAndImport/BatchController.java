package org.example.autopark.exportAndImport;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job importJob;

    @PostMapping("/api/managers/import-batch")
    public String startBatchImport() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(importJob, parameters);
            return "Импорт через Spring Batch запущен!";
        } catch (Exception e) {
            return "Ошибка запуска: " + e.getMessage();
        }
    }
}
