package org.example.autopark.gatling;

import io.gatling.javaapi.core.*;
import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class GetVehicleSimulation extends BaseSimulation {
    public GetVehicleSimulation() {
        int targetRps = Integer.getInteger("rate", 200);
        int warmupSec = Integer.getInteger("warmupSec", 30);
        int holdSec   = Integer.getInteger("durationSec", 180);

        ScenarioBuilder scn = scenario("GET /rps/api/vehicles/{id}")
                // ⬇️ НИКАКОЙ аутентификации для /rps/**
                .feed(vehiclesFeeder)
                // Быстрая проверка, чтобы не бегать с пустым vehicleId
                .exec(session -> {
                    if (!session.contains("vehicleId")) {
                        throw new RuntimeException("vehicleId missing from feeder/session");
                    }
                    return session;
                })
                .exec(
                        http("get-vehicle")
                                .get("/rps/api/vehicles/${vehicleId}")
                                .check(status().is(200))
                );

        setUp(scn.injectOpen(openRampThenHold(targetRps, warmupSec, holdSec)))
                .protocols(httpProtocol)
                .throttle(
                        reachRps(targetRps).in(Duration.ofSeconds(warmupSec)),
                        holdFor(Duration.ofSeconds(holdSec))
                )
                .assertions(
                        global().successfulRequests().percent().gt(99.0),
                        global().responseTime().percentile3().lt(800),
                        global().responseTime().percentile4().lt(2000)
                );
    }
}
