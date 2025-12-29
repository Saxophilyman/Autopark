package org.example.autopark.appUtil.trackGeneration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GpsPointCoord {
    private double lat; //y широта
    private double lng; //x долгота
}
