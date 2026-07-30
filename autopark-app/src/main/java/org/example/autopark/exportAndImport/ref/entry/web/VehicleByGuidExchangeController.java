package org.example.autopark.exportAndImport.ref.entry.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.exportAndImport.ref.application.byGuid.*;
import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.ref.format.DataFormat;
import org.example.autopark.exportAndImport.ref.format.byGuid.VehicleByGuidDocumentIO;
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
import java.util.UUID;

@RestController
@Profile("!reactive")
@RequestMapping("api/managers")
@RequiredArgsConstructor
@Tag(name = "Vehicle exchange by GUID", description = "Импорт, экспорт и preview автомобиля по GUID")
public class VehicleByGuidExchangeController {
    private final ExportVehicleByGuidService exportService;
    private final ImportVehicleByGuidService importService;
    private final VehicleByGuidDocumentIO documentIO;
    private final DownloadResponseConfigurer responseConfigurer;

    @GetMapping("/export-guid/vehicle/{guid}")
    @Operation(summary = "Экспорт данных по ТС по GUID")
    public void exportVehicleByGuid(@Parameter(hidden = true) @CurrentManagerId Long managerId,
                                    @PathVariable UUID guid,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                    @RequestParam(defaultValue = "json") String format,
                                    @RequestParam(defaultValue = "false") boolean withTrack,
                                    HttpServletResponse response) throws IOException {
        DataFormat dataFormat = DataFormat.fromRequestParam(format);
        TrackMode trackMode = TrackMode.fromBoolean(withTrack);
        VehicleExportDtoByGuid document = exportService.execute(new ExportVehicleByGuidCommand(managerId, guid, fromDate, toDate, trackMode));

        responseConfigurer.prepare(response, "vehicle_" + guid, dataFormat);
        documentIO.write(dataFormat, document, response.getOutputStream());
    }

    @PostMapping("/import-guid")
    @Operation(summary = "Импорт данных по ТС по GUID")
    public ResponseEntity<String> importVehicleFromGuid(@Parameter(hidden = true) @CurrentManagerId Long managerId,
                                                         @RequestParam("file") MultipartFile file) {
        DataFormat format = DataFormat.fromFilename(file.getOriginalFilename());

        try (InputStream input = file.getInputStream()) {
            VehicleExportDtoByGuid document = documentIO.read(format, input);
            importService.execute(new ImportVehicleByGuidCommand(managerId, document));
            return ResponseEntity.ok("Файл успешно импортирован!");
        } catch (IOException exception) {
            throw new InvalidExchangeDocumentException("Не удалось прочитать файл импорта по GUID", exception);
        }
    }

    @GetMapping("/export-guid/json")
    @Operation(summary = "Предварительный просмотр GUID-экспорта как JSON")
    public ResponseEntity<VehicleExportDtoByGuid> exportGuidJsonResponse(@Parameter(hidden = true) @CurrentManagerId Long managerId,
                                                                         @RequestParam UUID vehicleGuid,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                         @RequestParam(defaultValue = "false") boolean withTrack) {
        VehicleExportDtoByGuid document = exportService.execute(new ExportVehicleByGuidCommand(
                managerId, vehicleGuid, fromDate, toDate, TrackMode.fromBoolean(withTrack)));
        return ResponseEntity.ok(document);
    }
}
