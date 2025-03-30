package org.example.autopark.exportAndImport.batchConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.autopark.exportAndImport.byGuid.ImportServiceByGuid;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.byID.ImportServiceById;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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

    private final ImportServiceById importServiceById;
    private final ImportServiceByGuid importServiceByGuid;

    // ======== IMPORT BY ID ========
    @Bean
    public JsonItemReader<VehicleExportDtoById> vehicleJsonReader() {
        return new JsonItemReaderBuilder<VehicleExportDtoById>()
                .name("vehicleJsonReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(VehicleExportDtoById.class))
                .resource(new FileSystemResource("data/vehicle-data.json"))
                .build();
    }

    @Bean
    public ItemProcessor<VehicleExportDtoById, VehicleExportDtoById> processorById() {
        return item -> item;
    }

    @Bean
    public ItemWriter<VehicleExportDtoById> writerById() {
        return items -> {
            for (VehicleExportDtoById dto : items) {
                importServiceById.importFromDtoById(dto);
            }
        };
    }

    @Bean
    public Step importVehicleByIdStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      ItemReader<VehicleExportDtoById> reader,
                                      ItemProcessor<VehicleExportDtoById, VehicleExportDtoById> processor,
                                      ItemWriter<VehicleExportDtoById> writer) {

        return new StepBuilder("importVehicleByIdStep", jobRepository)
                .<VehicleExportDtoById, VehicleExportDtoById>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importVehicleByIdJob(JobRepository jobRepository, Step importVehicleByIdStep) {
        return new JobBuilder("importVehicleByIdJob", jobRepository)
                .start(importVehicleByIdStep)
                .build();
    }

    // ======== IMPORT BY GUID ========
    @Bean
    public JsonItemReader<VehicleExportDtoByGuid> vehicleGuidJsonReader() {
        return new JsonItemReaderBuilder<VehicleExportDtoByGuid>()
                .name("vehicleGuidJsonReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(VehicleExportDtoByGuid.class))
                .resource(new FileSystemResource("data/vehicle-guid-data.json"))
                .build();
    }

    @Bean
    public ItemProcessor<VehicleExportDtoByGuid, VehicleExportDtoByGuid> processorByGuid() {
        return item -> item;
    }

    @Bean
    public ItemWriter<VehicleExportDtoByGuid> writerByGuid() {
        return items -> {
            for (VehicleExportDtoByGuid dto : items) {
                importServiceByGuid.importFromDtoByGuid(dto);
            }
        };
    }

    @Bean
    public Step importVehicleByGuidStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        ItemReader<VehicleExportDtoByGuid> reader,
                                        ItemProcessor<VehicleExportDtoByGuid, VehicleExportDtoByGuid> processor,
                                        ItemWriter<VehicleExportDtoByGuid> writer) {

        return new StepBuilder("importVehicleByGuidStep", jobRepository)
                .<VehicleExportDtoByGuid, VehicleExportDtoByGuid>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importVehicleByGuidJob(JobRepository jobRepository, Step importVehicleByGuidStep) {
        return new JobBuilder("importVehicleByGuidJob", jobRepository)
                .start(importVehicleByGuidStep)
                .build();
    }
}
