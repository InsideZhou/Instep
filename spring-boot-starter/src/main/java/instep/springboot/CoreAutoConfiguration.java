package instep.springboot;

import instep.Instep;
import instep.InstepLogger;
import instep.cache.Cache;
import instep.servicecontainer.ServiceContainer;
import instep.typeconversion.TypeConversion;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auto configuration for instep.core module.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
public class CoreAutoConfiguration {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @ConditionalOnMissingBean
    public Instep instep(
        @Autowired(required = false) ServiceContainer serviceContainer,
        @Autowired(required = false) Cache cache,
        @Autowired(required = false) TypeConversion typeConversion,
        @Autowired(required = false) InstepLogger instepLogger
    ) {
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
            InstepLogger.Companion.setLogger(instepLogger);
        }

        return Instep.INSTANCE;
    }

    @SuppressWarnings("NullableProblems")
    @Bean
    @ConditionalOnMissingBean
    public InstepLogger instepLogger() {
        return new InstepLogger() {
            @Override
            public boolean getEnableDebug() {
                return true;
            }

            @Override
            public boolean getEnableInfo() {
                return true;
            }

            @Override
            public boolean getEnableWarning() {
                return true;
            }

            @Override
            public void debug(String s, String s1) {
                LogFactory.getLog(s1).debug(s);
            }

            @Override
            public void info(String s, String s1) {
                LogFactory.getLog(s1).info(s);
            }

            @Override
            public void warning(String s, String s1) {
                LogFactory.getLog(s1).warn(s);
            }

            @Override
            public void debug(String s, Class<?> aClass) {
                debug(s, aClass.getName());
            }

            @Override
            public void info(String s, Class<?> aClass) {
                info(s, aClass.getName());
            }

            @Override
            public void warning(String s, Class<?> aClass) {
                warning(s, aClass.getName());
            }
        };
    }
}
