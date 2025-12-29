package org.example.notify.client;

import lombok.RequiredArgsConstructor;
import org.example.notify.model.VehicleBriefDto;
import org.example.notify.model.VehiclesSummaryDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AutoparkClient {

    private final RestClient rest;

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

    public String mileageReport(String plate, String from, String to, String period) {
        return rest.get()
                .uri(uri -> uri.path("/internal/tg/report/mileage")
                        .queryParam("licensePlate", plate)
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .queryParam("period", period)
                        .build())
                .retrieve()
                .body(String.class);
    }

    public Optional<VehicleBriefDto> getVehicleBrief(UUID vehicleGuid) {
        try {
            var dto = rest.get()
                    .uri("/api/notify/lookup/vehicle-brief?vehicleGuid={id}", vehicleGuid)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(VehicleBriefDto.class);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
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
