package org.example.autopark.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public abstract class BaseSimulation extends Simulation {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    protected static final int MAX_CONN_PER_HOST = Integer.getInteger("maxConnPerHost", 200);

    // если понадобится защищённая часть API
    protected static final String AUTH_USER = System.getProperty("authUser", "manager");
    protected static final String AUTH_PASS = System.getProperty("authPass", "manager");
    protected static final String STATIC_JWT = System.getProperty("jwt", "");

    protected final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .shareConnections()
            .maxConnectionsPerHost(MAX_CONN_PER_HOST);

    // фидер с id авто (берём из src/test/resources/data/vehicles.csv)
    protected final FeederBuilder<String> vehiclesFeeder =
            csv("data/vehicles.csv").circular();

    /** Логин только если нужен: /rps/** у тебя и так permitAll */
    protected ChainBuilder authenticateIfNeeded() {
        if (STATIC_JWT != null && !STATIC_JWT.isBlank()) {
            return exec(session -> session.set("token", STATIC_JWT));
        }
        // В твоём проекте /auth/login принимает form-параметры и отдаёт
        // токен простым текстом, НЕ JSON. Сохраняем весь body как token.
        return exec(
                http("login")
                        .post("/auth/login")
                        .formParam("username", AUTH_USER)
                        .formParam("password", AUTH_PASS)
                        .check(status().is(200))
                        .check(bodyString().saveAs("token"))
        );
    }

    /** удобная инъекция: разогрев до targetRps и удержание */
    protected static OpenInjectionStep[] openRampThenHold(int targetRps, int warmupSec, int holdSec) {
        return new OpenInjectionStep[]{
                rampUsersPerSec(1).to(targetRps).during(warmupSec),
                constantUsersPerSec(targetRps).during(holdSec)
        };
    }
}
