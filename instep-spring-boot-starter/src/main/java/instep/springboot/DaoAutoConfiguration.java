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
 * auto configuration for instep.dao module.
 */
@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringFacetCodeInspection"})
@Configuration
@ConditionalOnBean(Instep.class)
public class DaoAutoConfiguration {
    @Autowired(required = false)
    SQLPlanExecutor sqlPlanExecutor;

    @Autowired(required = false)
    ExpressionFactory expressionFactory;

    @Autowired(required = false)
    PlanFromTextFactory planFromTextFactory;

    @Autowired(required = false)
    ObjectSelectPlanFactory objectSelectPlanFactory;

    @Autowired(required = false)
    TableSelectPlanFactory tableSelectPlanFactory;

    @Autowired(required = false)
    TableInsertPlanFactory tableInsertPlanFactory;

    @Autowired(required = false)
    TableUpdatePlanFactory tableUpdatePlanFactory;

    @Autowired(required = false)
    TableDeletePlanFactory tableDeletePlanFactory;

    @Autowired(required = false)
    TableRowFactory tableRowFactory;

    @Bean
    public InstepSQL instepSQL() {
        if (null != sqlPlanExecutor) {
            Instep.INSTANCE.bind(SQLPlanExecutor.class, sqlPlanExecutor, "");
        }

        if (null != expressionFactory) {
            Instep.INSTANCE.bind(ExpressionFactory.class, expressionFactory, "");
        }

        if (null != planFromTextFactory) {
            Instep.INSTANCE.bind(PlanFromTextFactory.class, planFromTextFactory, "");
        }

        if (null != objectSelectPlanFactory) {
            Instep.INSTANCE.bind(ObjectSelectPlanFactory.class, objectSelectPlanFactory, "");
        }

        if (null != tableSelectPlanFactory) {
            Instep.INSTANCE.bind(TableInsertPlanFactory.class, tableInsertPlanFactory, "");
        }

        if (null != tableUpdatePlanFactory) {
            Instep.INSTANCE.bind(TableUpdatePlanFactory.class, tableUpdatePlanFactory, "");
        }

        if (null != tableDeletePlanFactory) {
            Instep.INSTANCE.bind(TableDeletePlanFactory.class, tableDeletePlanFactory, "");
        }

        if (null != tableRowFactory) {
            Instep.INSTANCE.bind(TableRowFactory.class, tableRowFactory, "");
        }

        return InstepSQL.INSTANCE;
    }
}
