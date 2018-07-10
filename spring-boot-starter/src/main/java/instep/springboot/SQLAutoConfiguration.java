package instep.springboot;

import instep.Instep;
import instep.dao.sql.ConnectionProvider;
import instep.dao.sql.InstepSQL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auto configuration for instep.dao.sql module.
 */
@Configuration
@ConditionalOnBean({ConnectionProvider.class})
public class SQLAutoConfiguration {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public InstepSQL instepSQL(ConnectionProvider connectionProvider, Instep instep) {
        instep.bind(ConnectionProvider.class, connectionProvider, "");

        return InstepSQL.INSTANCE;
    }
}
