package org.example.autopark.trackingRef.adapter.gpx;


import org.example.autopark.trackingRef.model.TelemetryPoint;
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

/**
 * Низкоуровневый DOM-парсер GPX.
 */
final class GpxParser {

    private GpxParser() {
    }

    static List<TelemetryPoint> parse(
            InputStream inputStream,
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<TelemetryPoint> points = new ArrayList<>();

        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            NodeList trackPoints = document.getElementsByTagName("trkpt");
            for (int i = 0; i < trackPoints.getLength(); i++) {
                Element element = (Element) trackPoints.item(i);

                double latitude =
                        Double.parseDouble(element.getAttribute("lat"));
                double longitude =
                        Double.parseDouble(element.getAttribute("lon"));

                NodeList timeNodes = element.getElementsByTagName("time");
                if (timeNodes.getLength() == 0) {
                    throw new IllegalArgumentException(
                            "GPX-точка без <time> недопустима"
                    );
                }

                Instant timestamp = Instant.parse(
                        timeNodes.item(0).getTextContent().trim()
                );
                LocalDateTime utcTime =
                        LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);

                if (utcTime.isBefore(start) || utcTime.isAfter(end)) {
                    throw new IllegalArgumentException(
                            "GPX-точка выходит за пределы диапазона поездки: "
                                    + utcTime
                    );
                }

                points.add(
                        new TelemetryPoint(
                                timestamp,
                                latitude,
                                longitude
                        )
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка разбора GPX-файла: "
                            + exception.getMessage(),
                    exception
            );
        }

        return points;
    }
}
