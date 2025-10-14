package org.example.autopark.appUtil.trackGeneration;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class TrackGenDTO {
    private Long idVehicle;
    private int lengthOfTrack;
    private String date;
}
