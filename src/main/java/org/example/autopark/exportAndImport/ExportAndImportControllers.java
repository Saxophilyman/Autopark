package org.example.autopark.exportAndImport;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.exportAndImport.byGuid.CsvGuidExportUtil;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.byID.CsvExportUtil;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("api/managers")
@RequiredArgsConstructor
public class ExportAndImportControllers {

    private final ExportAndImportService exportAndImportService;
    private final ObjectMapper objectMapper;

    @GetMapping("/export/vehicle/{vehicleId}")
    public void exportVehicleData(@CurrentManagerId
                                    @PathVariable Long vehicleId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                    @RequestParam(defaultValue = "json") String format,
                                    HttpServletResponse response) throws Exception {

        VehicleExportDtoById dto = exportAndImportService.exportDataById(vehicleId, fromDate, toDate);

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=vehicle_" + vehicleId + ".csv");
            CsvExportUtil.writeVehicleExportToCsv(dto, response.getOutputStream());
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=vehicle_" + vehicleId + ".json");
            objectMapper.writeValue(response.getOutputStream(), dto);
        }
    }


    @PostMapping("/import")
    public String importVehicleData(@CurrentManagerId @RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) throw new IllegalArgumentException("Файл без имени");

            if (filename.endsWith(".json")) {
                VehicleExportDtoById dto = objectMapper.readValue(file.getInputStream(), VehicleExportDtoById.class);
                exportAndImportService.importFromDtoById(dto);
            } else if (filename.endsWith(".csv")) {
                exportAndImportService.importFromCsv(file.getInputStream());
            } else {
                throw new IllegalArgumentException("Поддерживаются только JSON и CSV");
            }

            return "Файл успешно импортирован!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка импорта: " + e.getMessage();
        }
    }

    @GetMapping("/export-guid/vehicle/{guid}")
    public void exportVehicleByGuid(@CurrentManagerId @PathVariable UUID guid,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                    @RequestParam(defaultValue = "json") String format,
                                    HttpServletResponse response) throws IOException {

        VehicleExportDtoByGuid dto = exportAndImportService.exportDataByGuid(guid, fromDate, toDate);

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=vehicle_" + guid + ".csv");
            CsvGuidExportUtil.writeVehicleExportToCsvGuid(dto, response.getOutputStream());
        } else {
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment; filename=vehicle_" + guid + ".json");
            objectMapper.writeValue(response.getOutputStream(), dto);
        }
    }


    @PostMapping("/import-guid")
    public String importVehicleFromGuid(@CurrentManagerId @RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) throw new IllegalArgumentException("Файл без имени");

            if (filename.endsWith(".json")) {
                VehicleExportDtoByGuid dto = objectMapper.readValue(file.getInputStream(), VehicleExportDtoByGuid.class);
                exportAndImportService.importFromDtoByGuid(dto);
            } else if (filename.endsWith(".csv")) {
                exportAndImportService.importFromCsvGuid(file.getInputStream());
            } else {
                throw new IllegalArgumentException("Поддерживаются только JSON и CSV");
            }

            return "Файл успешно импортирован!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка импорта: " + e.getMessage();
        }
    }



}
