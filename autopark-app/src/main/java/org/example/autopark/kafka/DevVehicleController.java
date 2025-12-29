package org.example.autopark.kafka;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.repository.EnterpriseRepository; // проверь свой пакет/интерфейс
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Profile("!reactive")
public class DevVehicleController {

    private final VehicleRepository vehicles;
    private final EnterpriseRepository enterprises; // нужен для привязки предприятия
    private final ApplicationEventPublisher events;

    // CREATE: POST /dev/vehicle/new?licensePlate=X001XX77&name=Kalina&enterpriseId=1&managerId=1
    @PostMapping("/vehicle/new")
    @Transactional
    public String create(@RequestParam String licensePlate,
                         @RequestParam(required = false) String name,
                         @RequestParam Integer year,
                         @RequestParam(required = false) Long enterpriseId,
                         @RequestParam(required = false) UUID enterpriseGuid,
                         @RequestParam(required = false) Long managerId) {

        Vehicle v = new Vehicle();
        v.setLicensePlate(licensePlate);
        v.setVehicleName(Optional.ofNullable(name).orElse(licensePlate));
        v.setVehicleYearOfRelease(year);
        if (enterpriseId != null) {
            Enterprise e = enterprises.findById(enterpriseId).orElseThrow();
            v.setEnterpriseOwnerOfVehicle(e);
        } else if (enterpriseGuid != null) {
            Enterprise e = enterprises.findByGuid(enterpriseGuid).orElseThrow();
            v.setEnterpriseOwnerOfVehicle(e);
        }

        vehicles.save(v);

        publishSnapshot(v, managerId, VehicleDomainEvent.Action.CREATED);
        return v.getGuid().toString();
    }

    // UPDATE (rename): POST /dev/vehicle/{id}/rename?name=Kalina+2&managerId=1
    @PostMapping("/vehicle/{id}/rename")
    @Transactional
    public String rename(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) Long managerId) {
        Vehicle v = vehicles.findById(id).orElseThrow();
        v.setVehicleName(name);
        vehicles.save(v);

        publishSnapshot(v, managerId, VehicleDomainEvent.Action.UPDATED);
        return "ok";
    }

    // DELETE: POST /dev/vehicle/{id}/delete?managerId=1
    @PostMapping("/vehicle/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) Long managerId) {
        Vehicle v = vehicles.findById(id).orElseThrow();

        // Снимок ДО удаления
        publishSnapshot(v, managerId, VehicleDomainEvent.Action.DELETED);

        vehicles.delete(v);
        return "ok";
    }

    private void publishSnapshot(Vehicle v, Long managerId, VehicleDomainEvent.Action action) {
        UUID vehicleGuid = v.getGuid();
        Enterprise ent = v.getEnterpriseOwnerOfVehicle();
        UUID enterpriseGuid = ent != null ? ent.getGuid() : null;
        String enterpriseName = ent != null ? ent.getName() : null;

        Long mid = Optional.ofNullable(managerId).orElse(1L); // DEV default

        events.publishEvent(new VehicleDomainEvent(
                vehicleGuid,
                enterpriseGuid,
                mid,
                action,
                v.getLicensePlate(),
                v.getVehicleName(),
                enterpriseName
        ));
    }

    @PostMapping("/vehicle/delete-by-plate")
    @Transactional
    public String deleteByPlate(@RequestParam String licensePlate,
                                @RequestParam Long managerId) {
        var v = vehicles.findByLicensePlate(licensePlate).orElseThrow();

        // соберём всё, что нужно для события ДО удаления
        var enterprise = v.getEnterpriseOwnerOfVehicle();
        var vehicleGuid     = v.getGuid();
        var enterpriseGuid  = (enterprise != null) ? enterprise.getGuid() : null;
        var enterpriseName  = (enterprise != null) ? enterprise.getName() : null;
        var plate           = v.getLicensePlate();
        var name            = v.getVehicleName();

        // удаляем запись
        vehicles.delete(v);

        // публикуем доменное событие (уйдёт в Kafka ПОСЛЕ коммита)
        events.publishEvent(new VehicleDomainEvent(
                vehicleGuid,
                enterpriseGuid,
                managerId,
                VehicleDomainEvent.Action.DELETED,
                plate,
                name,
                enterpriseName
        ));
        return "ok";
    }
}
