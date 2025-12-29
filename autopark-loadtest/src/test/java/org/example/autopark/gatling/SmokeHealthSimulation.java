package org.example.autopark.gatling;

import io.gatling.javaapi.core.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SmokeHealthSimulation extends BaseSimulation {
    public SmokeHealthSimulation() {
        ScenarioBuilder scn = scenario("Smoke /actuator/health")
                .exec(http("health").get("/actuator/health").check(status().in(200, 204)));

        setUp(scn.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocol)
                .assertions(global().successfulRequests().percent().gt(99.0));
    }
}
