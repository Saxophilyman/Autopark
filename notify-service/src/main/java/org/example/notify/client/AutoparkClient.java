package org.example.notify.client;

import org.example.notify.model.VehicleBriefDto;
import org.example.notify.model.VehiclesSummaryDto;
import org.example.notify.telegram.report.ReportPeriod;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class AutoparkClient {

    private final RestClient rest;

    @Value("${internal.api.token:dev-token}")
    private String internalToken;

    public AutoparkClient(@Qualifier("autoparkRestClient") RestClient rest) {
        this.rest = rest;
    }

    public VehiclesSummaryDto getVehiclesSummary(Long managerId) {
        return rest.get()
                .uri("/api/notify/vehicles/summary?managerId={id}", managerId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(VehiclesSummaryDto.class);
    }

    public Optional<Long> findChatIdByManager(Long managerId) {
        try {
            Long chatId = rest.get()
                    .uri("/api/notify/tg/chatId?managerId={id}", managerId)
                    .retrieve()
                    .body(Long.class);
            return Optional.ofNullable(chatId);
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }

    public String mileageReport(Long managerId, String plate, LocalDate from, LocalDate to, ReportPeriod period) {
        return rest.get()
                .uri(uri -> uri.path("/internal/tg/report/mileage")
                        .queryParam("managerId", managerId)
                        .queryParam("licensePlate", plate)
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .queryParam("period", period.name())
                        .build())
                .retrieve()
                .body(String.class);
    }

    public Optional<VehicleBriefDto> lookupVehicleBrief(UUID vehicleGuid) {
        try {
            var dto = rest.get()
                    .uri("/api/notify/lookup/vehicle-brief?vehicleGuid={id}", vehicleGuid)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(VehicleBriefDto.class);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }
}
