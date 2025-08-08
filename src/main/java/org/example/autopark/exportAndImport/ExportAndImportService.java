package org.example.autopark.exportAndImport;

import lombok.RequiredArgsConstructor;
import org.example.autopark.exportAndImport.byGuid.CsvGuidImportUtil;
import org.example.autopark.exportAndImport.byGuid.ExportServiceByGuid;
import org.example.autopark.exportAndImport.byGuid.ImportServiceByGuid;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.byID.CsvImportUtil;
import org.example.autopark.exportAndImport.byID.ExportServiceById;
import org.example.autopark.exportAndImport.byID.ImportServiceById;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.example.autopark.util.TransactionHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
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
    private final TransactionHelper transactionHelper;



    public VehicleExportDtoById exportDataById(Long vehicleId, LocalDate fromDate, LocalDate toDate) {
        return exportServiceById.getVehicleExportDataById(vehicleId, fromDate, toDate);
    }

    public VehicleExportDtoByGuid exportDataByGuid(UUID vehicleGuid,
                                                   LocalDate fromDate,
                                                   LocalDate toDate,
                                                   boolean withTrack) {
        return exportServiceByGuid.exportDataByGuid
                (vehicleGuid, fromDate, toDate, withTrack);
    }


    public void importFromDtoById(VehicleExportDtoById dto) {
        transactionHelper.runInTransaction(() -> {
            importServiceById.importFromDtoById(dto);
        });
    }

    public void importFromDtoByGuid(VehicleExportDtoByGuid dto) {
        transactionHelper.runInTransaction(() -> {
            importServiceByGuid.importFromDtoByGuid(dto);
        });
    }

    public void importFromCsv(InputStream stream) throws IOException {
        transactionHelper.runInTransactionWithIOException(() -> csvImportUtil.importFromCsv(stream));
    }

    public void importFromCsvGuid(InputStream stream) throws IOException {
        transactionHelper.runInTransactionWithIOException(() -> csvGuidImportUtil.importFromCsvGuid(stream));
    }

}



