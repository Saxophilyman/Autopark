package org.example.autopark.nplusone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.VehicleRepository;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.autopark.nplusone.QueryCountHolder.*;

@Service
@RequiredArgsConstructor
@Profile("!reactive")
@Slf4j
public class NPlusOneDemoService {

    private final VehicleRepository repo;
    private final EntityManager em;

    /** Размер демо-выборки: небольшое число, чтобы логи и время были адекватными */
    private static final int SAMPLE_SIZE = 50;

    @Transactional(readOnly = true)
    public Map<String, Object> runAll() {
        var out = new LinkedHashMap<String, Object>();
        out.put("A_N+1_forced", runNPlusOneForced());   // «честный» N+1 без правки аннотаций
        out.put("B_FETCH",       runFetchJoin());
        out.put("C_BATCH",       runBatchOrSubselect());
        out.put("D_DTO",         runDto());
        return out;
    }

    private List<Long> pageOfIds() {
        return repo.findIds(PageRequest.of(0, SAMPLE_SIZE));
    }

    /** A) Форсируем N+1: чистим persistence context после каждой загрузки, ломая батч-группировки. */
    private Map<String, Object> runNPlusOneForced() {
        MDC.put("scenario", "A-N+1");
        try {
            reset();
            var ids = pageOfIds(); // 1 select по id
            long tripsTouched = 0;

            for (Long id : ids) {
                // Загружаем одну машину в свежем контексте
                Vehicle v = em.find(Vehicle.class, id);      // select vehicle by id
                tripsTouched += v.getTripList().size();      // select trips for this vehicle
                // Ломаем возможность батча: очищаем контекст → следующий find/getTrips будет в НОВОМ контексте
                em.clear();
            }

            var c = get();
            log.info("A) vehicles={}, tripsAccessed={}, selects={}", ids.size(), tripsTouched, c.select);
            return Map.of(
                    "vehicles", ids.size(),
                    "tripsAccessed", tripsTouched,
                    "selects", c.select,
                    "note", "Форсированный N+1: после каждой итерации em.clear(), батч/подзапрос не срабатывает"
            );
        } finally {
            MDC.remove("scenario");
        }
    }

    /** B) JOIN FETCH: один запрос. */
    private Map<String, Object> runFetchJoin() {
        MDC.put("scenario", "B-FETCH");
        try {
            reset();
            var vehicles = repo.findAllWithTripsFetch(); // SELECT ... LEFT JOIN FETCH ...
            // Ограничим расчёт «ради порядка» той же выборкой по id (чтобы сравнение было честнее)
            // (не обязательно, можно убрать этот кусок)
            long tripsTouched = vehicles.stream().limit(SAMPLE_SIZE)
                    .mapToLong(v -> v.getTripList().size()).sum();

            var c = get();
            log.info("B) vehicles={}, tripsAccessed={}, selects={}", vehicles.size(), tripsTouched, c.select);
            return Map.of(
                    "vehiclesTotal", vehicles.size(),
                    "vehiclesMeasured", Math.min(vehicles.size(), SAMPLE_SIZE),
                    "tripsAccessed", tripsTouched,
                    "selects", c.select,
                    "note", "JOIN FETCH: один SELECT вне зависимости от количества машин"
            );
        } finally {
            MDC.remove("scenario");
        }
    }

    /** C) Batch/Subselect: не чистим контекст → Hibernate сам сгруппирует загрузки коллекций. */
    private Map<String, Object> runBatchOrSubselect() {
        MDC.put("scenario", "C-BATCH");
        try {
            reset();
            var ids = pageOfIds(); // 1 select по id
            // Загружаем машины ОДНИМ контекстом
            var vehicles = repo.findAllById(ids); // 1 select по IN(ids)
            long tripsTouched = vehicles.stream().mapToLong(v -> v.getTripList().size()).sum(); // несколько IN(...) или SUBSELECT

            var c = get();
            log.info("C) vehicles={}, tripsAccessed={}, selects={}", vehicles.size(), tripsTouched, c.select);
            return Map.of(
                    "vehicles", vehicles.size(),
                    "tripsAccessed", tripsTouched,
                    "selects", c.select,
                    "note", "@BatchSize или SUBSELECT: немного SELECT'ов (значительно меньше, чем A)"
            );
        } finally {
            MDC.remove("scenario");
        }
    }

    /** D) DTO-проекция: один контролируемый запрос. */
    private Map<String, Object> runDto() {
        MDC.put("scenario", "D-DTO");
        try {
            reset();
            var rows = repo.listWithTripsCount(); // SELECT join + GROUP BY
            var c = get();
            log.info("D) rows={}, selects={}", rows.size(), c.select);
            return Map.of(
                    "rows", rows.size(),
                    "selects", c.select,
                    "note", "DTO: 1 SELECT (JOIN+GROUP BY), N+1 по определению отсутствует"
            );
        } finally {
            MDC.remove("scenario");
        }
    }
}