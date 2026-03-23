package org.example.autopark.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Перехватчик для RestTemplate, считающий 4xx/5xx ответы удалённых сервисов.
 */
@Slf4j
@RequiredArgsConstructor
public class MonitoringHttpClientInterceptor implements ClientHttpRequestInterceptor {

    private final MonitoringMetrics metrics;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        ClientHttpResponse response = execution.execute(request, body);

        try {
            HttpStatusCode status = response.getStatusCode();

            if (status.is4xxClientError()) {
                metrics.incrementHttpClient4xx();
                log.debug("HTTP client 4xx: {} {} -> {}",
                        request.getMethod(), request.getURI(), status.value());
            } else if (status.is5xxServerError()) {
                metrics.incrementHttpClient5xx();
                log.debug("HTTP client 5xx: {} {} -> {}",
                        request.getMethod(), request.getURI(), status.value());
            }
        } catch (Exception e) {
            // На всякий случай не ломаем запрос, если вдруг что-то пошло не так в мониторинге
            log.warn("Failed to process HTTP client status for monitoring", e);
        }

        return response;
    }
}
