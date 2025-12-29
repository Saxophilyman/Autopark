package org.example.autopark.exception;

public class VehicleNotFoundException extends RuntimeException{
    private final Long id;

    public VehicleNotFoundException(Long id) {
        super("Vehicle with id=" + id + " not found");
        this.id = id;
    }
}
