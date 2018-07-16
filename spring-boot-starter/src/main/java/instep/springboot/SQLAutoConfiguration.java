package instep.springboot;

import instep.Instep;
import instep.dao.sql.ConnectionProvider;
import instep.dao.sql.Dialect;
import instep.dao.sql.InstepSQL;
import instep.dao.sql.TransactionContext;
import instep.servicecontainer.ServiceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * auto configuration for instep.dao.sql module.
 */
@Configuration
public class SQLAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Dialect dialect(@Value("${spring.datasource.url}") String url, Instep instep) {
        Dialect dialect = Dialect.Companion.of(url);

        try {
            instep.make(Dialect.class);
        }
        catch (ServiceNotFoundException e) {
            instep.bind(Dialect.class, dialect);
        }

        return dialect;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @ConditionalOnMissingBean
    public InstepSQL instepSQL(DataSource dataSource, Dialect dialect, Instep instep) {
        instep.bind(ConnectionProvider.class, new TransactionContext.ConnectionProvider(dataSource, dialect), "");

        return InstepSQL.INSTANCE;
    }
}
