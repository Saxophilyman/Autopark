package org.example.autopark.exportAndImport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointsRepository;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.appUtil.trackGeneration.TrackGenService;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.byGuid.CsvGuidImportUtil;
import org.example.autopark.exportAndImport.byGuid.ExportServiceByGuid;
import org.example.autopark.exportAndImport.byGuid.ImportServiceByGuid;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.byID.CsvImportUtil;
import org.example.autopark.exportAndImport.byID.ExportServiceById;
import org.example.autopark.exportAndImport.byID.ImportServiceById;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.DriverRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportAndImportService {
    private final ExportServiceById exportServiceById;
    private final CsvImportUtil csvImportUtil;
    private final CsvGuidImportUtil csvGuidImportUtil;
    private final ExportServiceByGuid exportServiceByGuid;
    private final ImportServiceById importServiceById;
    private final ImportServiceByGuid importServiceByGuid;


    public VehicleExportDtoById exportDataById(Long vehicleId, LocalDate fromDate, LocalDate toDate) {
        return exportServiceById.getVehicleExportDataById(vehicleId, fromDate, toDate);
    }

//    public VehicleExportDtoByGuid exportDataByGuid(UUID vehicleGuid, LocalDate fromDate, LocalDate toDate) {
//        return exportServiceByGuid.getVehicleExportDataByGuid(vehicleGuid, fromDate, toDate);
//    }

    public VehicleExportDtoByGuid exportDataByGuid(UUID vehicleGuid,
                                                   LocalDate fromDate,
                                                   LocalDate toDate,
                                                   boolean withTrack) {
        return exportServiceByGuid.exportDataByGuid
                (vehicleGuid, fromDate, toDate, withTrack);
    }


    @Transactional
    public void importFromDtoById(VehicleExportDtoById dto) {
        importServiceById.importFromDtoById(dto);
    }

    @Transactional
    public void importFromDtoByGuid(VehicleExportDtoByGuid dto) {
        importServiceByGuid.importFromDtoByGuid(dto);
    }

    public void importFromCsv(InputStream stream) throws IOException {
        csvImportUtil.importFromCsv(stream);
    }

    public void importFromCsvGuid(InputStream stream) throws IOException {
        csvGuidImportUtil.importFromCsvGuid(stream);
    }
}



