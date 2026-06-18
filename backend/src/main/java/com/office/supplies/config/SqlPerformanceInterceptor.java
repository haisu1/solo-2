package com.office.supplies.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Properties;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
@Component
public class SqlPerformanceInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SqlPerformanceInterceptor.class);

    @Value("${mybatis-plus.slow-sql-threshold:1000}")
    private long slowSqlThreshold;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            String sql = statementHandler.getBoundSql().getSql();
            Object parameter = statementHandler.getBoundSql().getParameterObject();

            if (costTime > slowSqlThreshold) {
                logger.warn("Slow SQL detected - cost: {}ms, threshold: {}ms\nSQL: {}\nParams: {}",
                        costTime, slowSqlThreshold, formatSql(sql), parameter);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("SQL executed - cost: {}ms\nSQL: {}\nParams: {}",
                        costTime, formatSql(sql), parameter);
            }
        }
    }

    private String formatSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
