package instep.springboot;

import instep.Instep;
import instep.InstepLogger;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auto configuration for instep.core module.
 */
@Configuration
public class CoreAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Instep instep(InstepLogger instepLogger) {
        Instep.INSTANCE.bind(InstepLogger.class, instepLogger, "");
        InstepLogger.Companion.setLogger(instepLogger);

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
