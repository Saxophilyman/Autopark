package org.example.autopark.exportAndImport.byID;

import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.example.autopark.trip.TripDTO;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CsvExportUtil {
    public static void writeVehicleExportToCsv(VehicleExportDtoById dto, OutputStream out) {
        try {
            // Добавляем BOM для корректного отображения в Excel
            OutputStreamWriter writerWithBom = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writerWithBom.write('\uFEFF');

            PrintWriter writer = new PrintWriter(writerWithBom);
            // Шапка по предприятию и машине
            writer.println("Enterprise ID;Enterprise Name;City;TimeZone;Vehicle ID;Vehicle Name;Cost;Year;Brand");
            writer.printf("%d;%s;%s;%s;%d;%s;%s;%d;%d;%s\n",
                    dto.getEnterprise().getId(),
                    dto.getEnterprise().getName(),
                    dto.getEnterprise().getCity(),
                    dto.getEnterprise().getTimeZone(),
                    dto.getVehicle().getId(),
                    dto.getVehicle().getName(),
                    dto.getVehicle().getLicensePlate(),
                    dto.getVehicle().getCost(),
                    dto.getVehicle().getYearOfRelease(),
                    dto.getVehicle().getBrand()
            );

            // Заголовок для поездок
            writer.println();
            writer.println("Trip Start;Trip End;Start Location;End Location;Duration");

            for (TripDTO trip : dto.getTrips()) {
                writer.printf("%s;%s;%s;%s;%s\n",
                        trip.getStartDate(),
                        trip.getEndDate(),
                        trip.getStartLocationInString(),
                        trip.getEndLocationInString(),
                        trip.getDuration()
                );
            }


            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при записи CSV", e);
        }
    }
}
