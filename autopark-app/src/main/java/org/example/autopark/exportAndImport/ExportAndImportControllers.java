package org.example.autopark.exportAndImport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.exportAndImport.byGuid.CsvGuidExportUtil;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.byID.CsvExportUtil;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
@Slf4j
@RestController
@Profile("!reactive")
@RequestMapping("api/managers")
@RequiredArgsConstructor
@Tag(
        name = "Export & Import (Manager API)",
        description = "Экспорт и импорт данных по ТС (по ID и по GUID) в форматах JSON и CSV"
)
public class ExportAndImportControllers {

    private final ExportAndImportService exportAndImportService;
    private final ObjectMapper objectMapper;

    // EXPORT BY ID
    @GetMapping("/export/vehicle/{vehicleId}")
    @Operation(
            summary = "Экспорт данных по ТС (по ID)",
            description = """
                    Экспортирует данные по одному ТС (предприятие, машина, поездки) за период.
                    Формат ответа: JSON (по умолчанию) или CSV-файл.
                    """
    )
    public void exportVehicleData(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @Parameter(description = "ID транспортного средства", example = "1")
            @PathVariable Long vehicleId,

            @Parameter(description = "Дата начала периода", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Дата окончания периода", example = "2025-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @Parameter(description = "Формат: json или csv", example = "json")
            @RequestParam(defaultValue = "json") String format,

            HttpServletResponse response
    ) throws Exception {
        VehicleExportDtoById dto = exportAndImportService.exportDataById(vehicleId, fromDate, toDate);
        DataFormat dataFormat = DataFormat.fromRequestParam(format);
        writeVehicleByIdResponse(dto, vehicleId, dataFormat, response);
    }

    // IMPORT BY ID
    @PostMapping("/import")
    @Operation(
            summary = "Импорт данных по ТС (по ID)",
            description = """
                    Импортирует данные по одному ТС и его поездкам из файла JSON или CSV.
                    Формат файла должен соответствовать структуре, которую отдает экспорт по ID.
                    """
    )
    public String importVehicleData(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestParam("file") MultipartFile file
    ) {
        try {
            importVehicleById(file);
            return "Файл успешно импортирован!";
        } catch (Exception e) {
            log.error("Ошибка импорта данных по ТС по ID", e);
            return "Ошибка импорта: " + e.getMessage();
        }
    }

    // EXPORT BY GUID (файл JSON/CSV)
    @GetMapping("/export-guid/vehicle/{guid}")
    @Operation(
            summary = "Экспорт данных по ТС (по GUID)",
            description = """
                    Экспорт по GUID (устойчивый идентификатор). Можно выгрузить:
                    - только поездки (start/end + адреса),
                    - либо поездки с полным треком (withTrack=true).
                    Форматы: JSON или CSV.
                    """
    )
    public void exportVehicleByGuid(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @Parameter(description = "GUID транспортного средства")
            @PathVariable UUID guid,

            @Parameter(description = "Дата начала периода", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Дата окончания периода", example = "2025-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @Parameter(description = "Формат экспорта: json или csv", example = "json")
            @RequestParam(defaultValue = "json") String format,

            @Parameter(description = "Включать полный GPS-трек по каждой поездке", example = "false")
            @RequestParam(defaultValue = "false") boolean withTrack,

            HttpServletResponse response
    ) throws IOException {

        VehicleExportDtoByGuid dto = exportAndImportService.exportDataByGuid(guid, fromDate, toDate, withTrack);
        DataFormat dataFormat = DataFormat.fromRequestParam(format);
        writeVehicleByGuidResponse(dto, guid, dataFormat, response);
    }

    // IMPORT BY GUID
    @PostMapping("/import-guid")
    @Operation(
            summary = "Импорт данных по ТС (по GUID)",
            description = """
                    Импортирует данные по ТС и поездкам, используя GUID предприятия и машины.
                    Формат файла должен соответствовать структуре, которую отдает экспорт по GUID.
                    """
    )
    public String importVehicleFromGuid(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestParam("file") MultipartFile file
    ) {
        try {
            importVehicleByGuid(file);
            return "Файл успешно импортирован!";
        } catch (Exception e) {
            log.error("Ошибка импорта данных по ТС по GUID", e);
            return "Ошибка импорта: " + e.getMessage();
        }
    }

    // EXPORT BY GUID (чистый JSON-ответ, удобно для UI-просмотра)
    @GetMapping("/export-guid/json")
    @Operation(
            summary = "Экспорт по GUID (JSON-ответ)",
            description = """
                    Возвращает JSON-структуру VehicleExportDtoByGuid без скачивания файла.
                    Удобно для предварительного просмотра в UI (например, в модальном окне).
                    """
    )
    public ResponseEntity<VehicleExportDtoByGuid> exportGuidJsonResponse(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @Parameter(description = "GUID транспортного средства")
            @RequestParam UUID vehicleGuid,

            @Parameter(description = "Дата начала периода", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Дата окончания периода", example = "2025-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @Parameter(description = "Включать полный GPS-трек по каждой поездке", example = "false")
            @RequestParam(defaultValue = "false") boolean withTrack
    ) {
        VehicleExportDtoByGuid dto = exportAndImportService.exportDataByGuid(vehicleGuid, fromDate, toDate, withTrack);
        return ResponseEntity.ok(dto);
    }

    private void importVehicleById(MultipartFile file) throws IOException {
        DataFormat dataFormat = DataFormat.fromFilename(file.getOriginalFilename());

        switch (dataFormat) {
            case JSON -> {
                VehicleExportDtoById dto = objectMapper.readValue(file.getInputStream(), VehicleExportDtoById.class);
                exportAndImportService.importFromDtoById(dto);
            }
            case CSV -> exportAndImportService.importFromCsv(file.getInputStream());
        }
    }

    private void importVehicleByGuid(MultipartFile file) throws IOException {
        DataFormat dataFormat = DataFormat.fromFilename(file.getOriginalFilename());

        switch (dataFormat) {
            case JSON -> {
                VehicleExportDtoByGuid dto = objectMapper.readValue(file.getInputStream(), VehicleExportDtoByGuid.class);
                exportAndImportService.importFromDtoByGuid(dto);
            }
            case CSV -> exportAndImportService.importFromCsvGuid(file.getInputStream());
        }
    }

    private void writeVehicleByIdResponse(VehicleExportDtoById dto, Long vehicleId,
                                          DataFormat dataFormat, HttpServletResponse response) throws IOException {
        String baseFilename = "vehicle_" + vehicleId;
        prepareDownloadResponse(response, baseFilename, dataFormat);

        switch (dataFormat){
            case JSON -> objectMapper.writeValue(response.getOutputStream(), dto);
            case CSV -> CsvExportUtil.writeVehicleExportToCsv(dto, response.getOutputStream());
        }
    }

    private void writeVehicleByGuidResponse(VehicleExportDtoByGuid dto, UUID guid,
                                            DataFormat dataFormat, HttpServletResponse response) throws IOException {
        String baseFilename = "vehicle_" + guid;
        prepareDownloadResponse(response, baseFilename, dataFormat);

        switch (dataFormat){
            case JSON -> objectMapper.writeValue(response.getOutputStream(), dto);
            case CSV -> CsvGuidExportUtil.writeVehicleExportToCsvGuid(dto, response.getOutputStream());
        }
    }


    private void prepareDownloadResponse(HttpServletResponse response, String baseFilename, DataFormat dataFormat) {
        response.setContentType(dataFormat.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=" + baseFilename + "." + dataFormat.getExtension());
    }

}
