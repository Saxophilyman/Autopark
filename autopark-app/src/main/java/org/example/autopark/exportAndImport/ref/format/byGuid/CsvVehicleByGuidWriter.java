package org.example.autopark.exportAndImport.ref.format.byGuid;


import org.example.autopark.exportAndImport.ref.document.byGuid.TripGuidExportDto;
import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Component
@Profile("!reactive")
public class CsvVehicleByGuidWriter {

    public void write(VehicleExportDtoByGuid document, OutputStream output) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.write("Enterprise GUID;Enterprise Name;City;TimeZone;Vehicle GUID;Vehicle Name;LicensePlate;Cost;Year;Brand");
            writer.newLine();
            writer.write(String.format("%s;%s;%s;%s;%s;%s;%s;%d;%d;%s",
                    document.getEnterprise().getGuid(), document.getEnterprise().getName(), document.getEnterprise().getCity(),
                    document.getEnterprise().getTimeZone(), document.getVehicle().getGuid(), document.getVehicle().getName(),
                    document.getVehicle().getLicensePlate(), document.getVehicle().getCost(), document.getVehicle().getYearOfRelease(),
                    document.getVehicle().getBrand()));
            writer.newLine();
            writer.newLine();
            writer.write("Trip GUID;Trip Start;Trip End;Start Location;End Location;Duration");
            writer.newLine();

            if (document.getTrips() != null) {
                for (TripGuidExportDto trip : document.getTrips()) {
                    writer.write(String.format("%s;%s;%s;%s;%s;%s", trip.getGuid(), trip.getStartTime(), trip.getEndTime(),
                            trip.getStartLocationInString(), trip.getEndLocationInString(), trip.getDuration()));
                    writer.newLine();
                }
            }
        }
    }
}
