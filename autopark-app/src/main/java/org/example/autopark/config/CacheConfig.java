package org.example.autopark.config;

import com.google.common.cache.CacheBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Configuration
@Profile("!reactive")
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("vehicles", "drivers", "enterprises", "trips", "mileageReports", "brandByName") {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(
                        name,
                        CacheBuilder.newBuilder()
                                .expireAfterWrite(30, TimeUnit.DAYS)        // удалять через N дней после записи
                                .expireAfterAccess(7, TimeUnit.DAYS)      // или через N дней простоя
                                .maximumSize(1000)                        // максимум 1000 записей
                                .build()
                                .asMap(),
                        false
                );
            }
        };
    }
}
