package instep.springboot;

import instep.Instep;
import instep.dao.ExpressionFactory;
import instep.dao.PlanFromTextFactory;
import instep.dao.sql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auto configuration for instep.dao.sql module.
 */
@SuppressWarnings({"SpringFacetCodeInspection", "SpringAutowiredFieldsWarningInspection"})
@Configuration
@ConditionalOnBean({ConnectionProvider.class})
public class SQLAutoConfiguration {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    public InstepSQL instepSQL(
        @SuppressWarnings("SpringJavaAutowiringInspection") ConnectionProvider connectionProvider,
        @SuppressWarnings("SpringJavaAutowiringInspection") Instep instep,
        @Autowired(required = false) SQLPlanExecutor sqlPlanExecutor,
        @Autowired(required = false) ExpressionFactory expressionFactory,
        @Autowired(required = false) PlanFromTextFactory planFromTextFactory,
        @Autowired(required = false) ObjectSelectPlanFactory objectSelectPlanFactory,
        @Autowired(required = false) TableSelectPlanFactory tableSelectPlanFactory,
        @Autowired(required = false) TableInsertPlanFactory tableInsertPlanFactory,
        @Autowired(required = false) TableUpdatePlanFactory tableUpdatePlanFactory,
        @Autowired(required = false) TableDeletePlanFactory tableDeletePlanFactory,
        @Autowired(required = false) TableRowFactory tableRowFactory
    ) {
        instep.bind(ConnectionProvider.class, connectionProvider, "");

        if (null != sqlPlanExecutor) {
            instep.bind(SQLPlanExecutor.class, sqlPlanExecutor, "");
        }

        if (null != expressionFactory) {
            instep.bind(ExpressionFactory.class, expressionFactory, "");
        }

        if (null != planFromTextFactory) {
            instep.bind(PlanFromTextFactory.class, planFromTextFactory, "");
        }

        if (null != objectSelectPlanFactory) {
            instep.bind(ObjectSelectPlanFactory.class, objectSelectPlanFactory, "");
        }

        if (null != tableSelectPlanFactory) {
            instep.bind(TableInsertPlanFactory.class, tableInsertPlanFactory, "");
        }

        if (null != tableUpdatePlanFactory) {
            instep.bind(TableUpdatePlanFactory.class, tableUpdatePlanFactory, "");
        }

        if (null != tableDeletePlanFactory) {
            instep.bind(TableDeletePlanFactory.class, tableDeletePlanFactory, "");
        }

        if (null != tableRowFactory) {
            instep.bind(TableRowFactory.class, tableRowFactory, "");
        }

        return InstepSQL.INSTANCE;
    }
}
