package nl.bronsenproject.dispatcher;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the {@link Dispatcher} correctly identifies and reports duplicate handlers for the same request type.
 */
public class DispatcherDuplicateHandlerTest {

    @Test
    void testDuplicateHandlerRegistration() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(DuplicateConfig.class);
        
        // The exception happens during discoverHandlers() which is called on the first send()
        // OR when the Dispatcher bean is initialized if we were to trigger it.
        // Since discoverHandlers is lazy, we need to trigger it.
        
        context.refresh();
        Dispatcher dispatcher = context.getBean(Dispatcher.class);
        
        RequestA request = new RequestA();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> dispatcher.send(request));
        
        assertTrue(exception.getMessage().contains("Multiple handlers registered for request type"));
        assertTrue(exception.getMessage().contains(RequestA.class.getName()));
        
        context.close();
    }

    @Configuration
    static class DuplicateConfig {
        @Bean
        public Dispatcher dispatcher(org.springframework.context.ApplicationContext context) {
            return new Dispatcher(context, null);
        }

        @Bean
        public HandlerA1 handlerA1() {
            return new HandlerA1();
        }

        @Bean
        public HandlerA2 handlerA2() {
            return new HandlerA2();
        }
    }

    public static class RequestA implements Request<String> {}

    public static class HandlerA1 implements TypedHandler<RequestA, String> {
        @Override
        public String handle(RequestA request) { return "1"; }
    }

    public static class HandlerA2 implements TypedHandler<RequestA, String> {
        @Override
        public String handle(RequestA request) { return "2"; }
    }
}
