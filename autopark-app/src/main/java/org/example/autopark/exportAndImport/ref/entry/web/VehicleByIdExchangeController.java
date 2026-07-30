package org.example.autopark.exportAndImport.ref.entry.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;

import org.example.autopark.exportAndImport.ref.application.byID.ExportVehicleByIdCommand;
import org.example.autopark.exportAndImport.ref.application.byID.ExportVehicleByIdService;
import org.example.autopark.exportAndImport.ref.application.byID.ImportVehicleByIdCommand;
import org.example.autopark.exportAndImport.ref.application.byID.ImportVehicleByIdService;
import org.example.autopark.exportAndImport.ref.document.byID.VehicleExportDtoById;
import org.example.autopark.exportAndImport.ref.format.DataFormat;
import org.example.autopark.exportAndImport.ref.format.byID.VehicleByIdDocumentIO;
import org.example.autopark.exportAndImport.ref.format.exception.InvalidExchangeDocumentException;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@RestController
@Profile("!reactive")
@RequestMapping("api/managers")
@RequiredArgsConstructor
@Tag(name = "Vehicle exchange by ID", description = "Локальный импорт и экспорт автомобиля по ID")
public class VehicleByIdExchangeController {
    private final ExportVehicleByIdService exportService;
    private final ImportVehicleByIdService importService;
    private final VehicleByIdDocumentIO documentIO;
    private final DownloadResponseConfigurer responseConfigurer;

    @GetMapping("/export/vehicle/{vehicleId}")
    @Operation(summary = "Экспорт данных по ТС по ID")
    public void exportVehicleData(@Parameter(hidden = true) @CurrentManagerId Long managerId,
                                  @PathVariable Long vehicleId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                  @RequestParam(defaultValue = "json") String format,
                                  HttpServletResponse response) throws IOException {
        DataFormat dataFormat = DataFormat.fromRequestParam(format);
        VehicleExportDtoById document = exportService.execute(new ExportVehicleByIdCommand(managerId, vehicleId, fromDate, toDate));

        responseConfigurer.prepare(response, "vehicle_" + vehicleId, dataFormat);
        documentIO.write(dataFormat, document, response.getOutputStream());
    }

    @PostMapping("/import")
    @Operation(summary = "Импорт данных по ТС по ID")
    public ResponseEntity<String> importVehicleData(@Parameter(hidden = true) @CurrentManagerId Long managerId,
                                                     @RequestParam("file") MultipartFile file) {
        DataFormat format = DataFormat.fromFilename(file.getOriginalFilename());

        try (InputStream input = file.getInputStream()) {
            VehicleExportDtoById document = documentIO.read(format, input);
            importService.execute(new ImportVehicleByIdCommand(managerId, document));
            return ResponseEntity.ok("Файл успешно импортирован!");
        } catch (IOException exception) {
            throw new InvalidExchangeDocumentException("Не удалось прочитать файл импорта по ID", exception);
        }
    }
}
