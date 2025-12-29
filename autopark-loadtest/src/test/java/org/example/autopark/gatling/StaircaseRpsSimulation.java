package org.example.autopark.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Один прогон с поэтапной нагрузкой (стейркейс/лестница) 600→800→1000 rps.
 * Параметры можно менять системными свойствами:
 * -Dsteps=600,800,1000  -DstepRampSec=45  -DstepHoldSec=90
 * -DpathPrefix=/rps     -DidColumn=vehicleId  -DfeedFile=data/vehicles.csv
 * -Dvus=200             -DmaxConnPerHost=600  -DbaseUrl=http://127.0.0.1:8080
 */
public class StaircaseRpsSimulation extends BaseSimulation {

    public StaircaseRpsSimulation() {

        // --------- читаем параметры с дефолтами ----------
        String stepsProp = System.getProperty("steps", "600,800,1000");
        List<Integer> steps = Arrays.stream(stepsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        int stepRampSec = Integer.getInteger("stepRampSec", 45);
        int stepHoldSec = Integer.getInteger("stepHoldSec", 90);

        String pathPrefix = System.getProperty("pathPrefix", "/rps");          // "/rps" или "/api"
        String idColumn   = System.getProperty("idColumn", "vehicleId");       // имя колонки в CSV
        String feedFile   = System.getProperty("feedFile", "data/vehicles.csv");

        // Сколько одновременных VU запустить (юзеры крутятся в forever-цикле).
        // Дефолт: максимум(100, половина пикового rps). Можно переопределить -Dvus=...
        int maxRps = steps.stream().mapToInt(i -> i).max().orElse(600);
        int vus    = Integer.getInteger("vus", Math.max(100, maxRps / 2));

        // общая длительность = (ramp+hold) * кол-во ступеней
        int totalSec = steps.size() * (stepRampSec + stepHoldSec);

        // --------- HTTP протокол (уже задан в BaseSimulation, просто используем) ----------
        HttpProtocolBuilder proto = httpProtocol; // из BaseSimulation

        // --------- фидер с id-шниками ----------
        FeederBuilder<String> feeder = csv(feedFile).circular();

        // --------- сценарий: бесконечный цикл GET по /{id} ----------
        ScenarioBuilder scn =
                scenario("Staircase GET " + pathPrefix + "/api/vehicles/{id}")
                        .forever().on(
                                feed(feeder)
                                        .exec(
                                                http("get-vehicle")
                                                        .get(pathPrefix + "/api/vehicles/${" + idColumn + "}")
                                                        // если JWT передали как -Djwt=..., то BaseSimulation сам положит токен в сессию
                                                        .header("Authorization", session -> {
                                                            String t = session.getString("token");
                                                            return (t == null || t.isBlank()) ? "" : "Bearer " + t;
                                                        })
                                                        .check(status().is(200))
                                        )
                        );

        // --------- формируем "лестницу" троттлинга ----------
        List<ThrottleStep> throttleSteps = new ArrayList<>();
        for (int step : steps) {
            throttleSteps.add(reachRps(step).in(Duration.ofSeconds(stepRampSec)));
            throttleSteps.add(holdFor(Duration.ofSeconds(stepHoldSec)));
        }

        // --------- сетап: поднимаем VU "одномоментно", даем им крутиться forever, ограничиваем maxDuration и троттлим RPS ----------
        setUp(
                scn.injectOpen(atOnceUsers(vus))
        )
                .protocols(proto)
                .throttle(throttleSteps.toArray(new ThrottleStep[0]))
                .maxDuration(Duration.ofSeconds(totalSec))
                .assertions(
                        // Глобальные критерии прохождения
                        global().successfulRequests().percent().gt(99.0),
                        global().responseTime().percentile3().lt(800),
                        global().responseTime().percentile4().lt(2000)
                );
    }
}
