package nl.bronsenproject.dispatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import java.util.List;

/**
 * Auto-configuration for the Dispatcher.
 * This class provides the necessary Spring beans to enable the Dispatcher in a Spring Boot application.
 */
@Configuration
public class DispatcherAutoConfiguration {

    /**
     * Default constructor for DispatcherAutoConfiguration.
     */
    public DispatcherAutoConfiguration() {
    }

    /**
     * Creates a {@link Dispatcher} bean.
     *
     * @param context   the application context used for handler discovery
     * @param behaviors the list of behaviors to be used by the dispatcher
     * @return the configured Dispatcher bean
     */
    @Bean
    public Dispatcher dispatcher(ApplicationContext context, List<Behavior> behaviors) {
        return new Dispatcher(context, behaviors);
    }
}
