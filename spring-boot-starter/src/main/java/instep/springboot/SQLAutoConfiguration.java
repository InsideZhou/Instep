package instep.springboot;

import instep.Instep;
import instep.dao.sql.ConnectionProvider;
import instep.dao.sql.Dialect;
import instep.dao.sql.InstepSQL;
import instep.dao.sql.TransactionContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * auto configuration for instep.dao.sql module.
 */
@Configuration
public class SQLAutoConfiguration {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @ConditionalOnMissingBean
    public InstepSQL instepSQL(DataSource dataSource, Dialect dialect, Instep instep) {
        instep.bind(ConnectionProvider.class, new TransactionContext.ConnectionProvider(dataSource, dialect), "");

        return InstepSQL.INSTANCE;
    }
}
