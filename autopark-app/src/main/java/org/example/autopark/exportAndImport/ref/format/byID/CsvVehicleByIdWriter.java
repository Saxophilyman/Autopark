package org.example.autopark.exportAndImport.ref.format.byID;


import org.example.autopark.exportAndImport.ref.document.byID.VehicleExportDtoById;
import org.example.autopark.trip.TripDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Component
@Profile("!reactive")
public class CsvVehicleByIdWriter {

    public void write(VehicleExportDtoById document, OutputStream output) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.write("Enterprise ID;Enterprise Name;City;TimeZone;Vehicle ID;Vehicle Name;LicensePlate;Cost;Year;Brand");
            writer.newLine();
            writer.write(String.format("%d;%s;%s;%s;%d;%s;%s;%d;%d;%s",
                    document.getEnterprise().getId(), document.getEnterprise().getName(), document.getEnterprise().getCity(),
                    document.getEnterprise().getTimeZone(), document.getVehicle().getId(), document.getVehicle().getName(),
                    document.getVehicle().getLicensePlate(), document.getVehicle().getCost(), document.getVehicle().getYearOfRelease(),
                    document.getVehicle().getBrand()));
            writer.newLine();
            writer.newLine();
            writer.write("Trip Start;Trip End;Start Location;End Location;Duration");
            writer.newLine();

            if (document.getTrips() != null) {
                for (TripDTO trip : document.getTrips()) {
                    writer.write(String.format("%s;%s;%s;%s;%s", trip.getStartDate(), trip.getEndDate(),
                            trip.getStartLocationInString(), trip.getEndLocationInString(), trip.getDuration()));
                    writer.newLine();
                }
            }
        }
    }
}
