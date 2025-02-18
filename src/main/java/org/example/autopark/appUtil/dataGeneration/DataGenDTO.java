package org.example.autopark.appUtil.dataGeneration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class DataGenDTO {
    private List<Long> enterprisesID;
    private int numberOfVehicle;
    private int numberOfDriver;
    private int indicatorOfActiveVehicle;
}
