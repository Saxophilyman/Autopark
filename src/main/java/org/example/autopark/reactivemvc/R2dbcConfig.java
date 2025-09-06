//package org.example.autopark.reactivemvc;
//
//import io.r2dbc.spi.ConnectionFactories;
//import io.r2dbc.spi.ConnectionFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
//import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
//import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
//import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
//import org.springframework.data.r2dbc.dialect.PostgresDialect;
//import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
//import org.springframework.r2dbc.core.DatabaseClient;
//
//@Configuration
//@EnableR2dbcRepositories(
//        basePackages = "org.example.autopark.reactivemvc", // только здесь!
//        entityOperationsRef = "r2dbcEntityOperations"
//)
//public class R2dbcConfig {
//
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        return ConnectionFactories.get("r2dbc:postgresql://localhost:5432/autopark");
//    }
//
//    @Bean
//    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
//        return new R2dbcEntityTemplate(connectionFactory);
//    }
//
//    @Bean
//    public ReactiveDataAccessStrategy dataAccessStrategy() {
//        return new DefaultReactiveDataAccessStrategy(PostgresDialect.INSTANCE);
//    }
//
//    @Bean
//    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
//        return DatabaseClient.create(connectionFactory);
//    }
//
//    @Bean
//    public R2dbcEntityOperations r2dbcEntityOperations(DatabaseClient databaseClient) {
//        return new R2dbcEntityTemplate(databaseClient, dataAccessStrategy());
//    }
//}
