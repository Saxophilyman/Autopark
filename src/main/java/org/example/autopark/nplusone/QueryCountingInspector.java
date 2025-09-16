package org.example.autopark.nplusone;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class QueryCountingInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        if (sql != null && sql.trim().toLowerCase().startsWith("select")) {
            QueryCountHolder.incrementSelect();
        }
        return sql; // обязательно возвращаем обратно!
    }
}
