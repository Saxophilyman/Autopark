package org.example.autopark.exportAndImport;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

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

        VehicleExportDto dto = exportAndImportService.getVehicleExportData(vehicleId, fromDate, toDate);

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
    public String importVehicleData(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) throw new IllegalArgumentException("Файл без имени");

            if (filename.endsWith(".json")) {
                VehicleExportDto dto = objectMapper.readValue(file.getInputStream(), VehicleExportDto.class);
                exportAndImportService.importFromDto(dto);
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


}
