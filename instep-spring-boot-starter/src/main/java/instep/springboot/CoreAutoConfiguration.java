package instep.springboot;

import instep.Instep;
import instep.InstepLogger;
import instep.cache.Cache;
import instep.servicecontainer.ServiceContainer;
import instep.typeconversion.TypeConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auto configuration for instep.core module.
 */
@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringFacetCodeInspection"})
@Configuration
public class CoreAutoConfiguration {
    @Autowired(required = false)
    private ServiceContainer serviceContainer;

    @Autowired(required = false)
    private Cache cache;

    @Autowired(required = false)
    private TypeConversion typeConversion;

    @Autowired(required = false)
    private InstepLogger instepLogger;

    @Bean
    public Instep instep() {
        if (null != serviceContainer) {
            Instep.INSTANCE.setServiceContainer(serviceContainer);
        }

        if (null != cache) {
            Instep.INSTANCE.bind(Cache.class, cache, "");
        }

        if (null != typeConversion) {
            Instep.INSTANCE.bind(TypeConversion.class, typeConversion, "");
        }

        if (null != instepLogger) {
            Instep.INSTANCE.bind(InstepLogger.class, instepLogger, "");
            InstepLogger.Companion.setRootLogger(instepLogger);
        }

        return Instep.INSTANCE;
    }
}
