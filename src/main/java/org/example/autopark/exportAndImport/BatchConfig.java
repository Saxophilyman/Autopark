package org.example.autopark.exportAndImport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final ExportAndImportService importService;
    private final ObjectMapper objectMapper;

    @Bean
    public JsonItemReader<VehicleExportDto> vehicleJsonReader() {
        return new JsonItemReaderBuilder<VehicleExportDto>()
                .name("vehicleJsonReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(VehicleExportDto.class))
                .resource(new FileSystemResource("data/vehicle-data.json"))
                .build();
    }

    @Bean
    public ItemProcessor<VehicleExportDto, VehicleExportDto> processor() {
        return item -> item;
    }

    @Bean
    public ItemWriter<VehicleExportDto> writer() {
        return items -> {
            for (VehicleExportDto dto : items) {
                importService.importFromDto(dto);
            }
        };
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<VehicleExportDto> reader,
                           ItemProcessor<VehicleExportDto, VehicleExportDto> processor,
                           ItemWriter<VehicleExportDto> writer) {

        return new StepBuilder("importStep", jobRepository)
                .<VehicleExportDto, VehicleExportDto>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importJob(JobRepository jobRepository, Step importStep) {
        return new JobBuilder("importVehicleJob", jobRepository)
                .start(importStep)
                .build();
    }
}
