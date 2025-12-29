package org.example.autopark.gatling;

import io.gatling.javaapi.core.*;
import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class MixedReadSimulation extends BaseSimulation {
    public MixedReadSimulation() {
        int targetRps = Integer.getInteger("rate", 200);
        int warmupSec = Integer.getInteger("warmupSec", 45);
        int holdSec   = Integer.getInteger("durationSec", 300);

        ScenarioBuilder scn = scenario("Mixed 80/20 vehicles/json")
                .exec(authenticateIfNeeded())
                .feed(vehiclesFeeder)
                .randomSwitch().on(
                        Choice.withWeight(80d,
                                exec(http("get-vehicle")
                                        .get("/rps/api/vehicles/${vehicleId}")
                                        .header("Authorization", session -> {
                                            String t = session.getString("token");
                                            return (t == null || t.isBlank()) ? "" : "Bearer " + t;
                                        })
                                        .check(status().is(200))
                                )
                        ),
                        Choice.withWeight(20d,
                                exec(http("json")
                                        .get("/rps/json")
                                        .check(status().is(200))
                                )
                        )
                );

        setUp(scn.injectOpen(openRampThenHold(targetRps, warmupSec, holdSec)))
                .protocols(httpProtocol)
                .throttle(reachRps(targetRps).in(Duration.ofSeconds(warmupSec)),
                        holdFor(Duration.ofSeconds(holdSec)))
                .assertions(
                        global().successfulRequests().percent().gt(99.0),
                        global().responseTime().percentile3().lt(800),
                        global().responseTime().percentile4().lt(2000)
                );
    }
}
