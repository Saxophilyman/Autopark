package org.example.autopark.gpx;

import org.example.autopark.gps.GpsPointDto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class GpxParser {

    /**
     * Разбирает GPX-файл, извлекая трек-точки и проверяя, что они укладываются в указанный временной диапазон.
     *
     * @param inputStream входной поток GPX-файла
     * @param start начало диапазона поездки
     * @param end конец диапазона поездки
     * @return список DTO-точек
     */
    public static List<GpsPointDto> parseGpx(InputStream inputStream, LocalDateTime start, LocalDateTime end) {
        List<GpsPointDto> points = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList trkptList = doc.getElementsByTagName("trkpt");
            for (int i = 0; i < trkptList.getLength(); i++) {
                Element trkpt = (Element) trkptList.item(i);

                double lat = Double.parseDouble(trkpt.getAttribute("lat"));
                double lon = Double.parseDouble(trkpt.getAttribute("lon"));

                NodeList timeNodes = trkpt.getElementsByTagName("time");
                if (timeNodes.getLength() == 0) {
                    throw new IllegalArgumentException("GPX-точка без <time> недопустима");
                }

                String timeStr = timeNodes.item(0).getTextContent().trim();
                Instant instant = Instant.parse(timeStr); // GPX использует ISO 8601 UTC
                LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

                if (time.isBefore(start) || time.isAfter(end)) {
                    throw new IllegalArgumentException("GPX-точка выходит за пределы диапазона поездки: " + time);
                }

                GpsPointDto point = new GpsPointDto();
                point.setLatitude(lat);
                point.setLongitude(lon);
                point.setTimestamp(time);
                point.setVehicleId(null);    // Пока неизвестно
                point.setGpsPointId(null);   // Авто-генерация при сохранении

                points.add(point);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка разбора GPX-файла: " + e.getMessage(), e);
        }

        return points;
    }
}
