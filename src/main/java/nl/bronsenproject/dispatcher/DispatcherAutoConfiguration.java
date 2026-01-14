package nl.bronsenproject.dispatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import java.util.List;

@Configuration
public class DispatcherAutoConfiguration {

    @Bean
    public Dispatcher dispatcher(ApplicationContext context, List<Behavior> behaviors) {
        return new Dispatcher(context, behaviors);
    }
}
