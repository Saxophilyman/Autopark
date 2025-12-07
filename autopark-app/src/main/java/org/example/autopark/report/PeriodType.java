package org.example.autopark.report;

import lombok.Getter;

@Getter
public enum PeriodType {
    DAY("День"),
    MONTH("Месяц"),
    YEAR("Год");

    private final String displayName;

    PeriodType(String displayName) {
        this.displayName = displayName;
    }

}
