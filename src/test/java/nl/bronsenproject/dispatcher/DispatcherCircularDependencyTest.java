package nl.bronsenproject.dispatcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link Dispatcher} for circular dependency handling.
 * Specifically checks if a handler can have the dispatcher injected.
 */
@SpringJUnitConfig(DispatcherCircularDependencyTest.Config.class)
public class DispatcherCircularDependencyTest {

    @Autowired
    private Dispatcher dispatcher;

    @Test
    void testCircularDependency() {
        CircularRequest request = new CircularRequest();
        CircularResult result = dispatcher.send(request);
        assertNotNull(result);
        assertTrue(result.success());
    }

    @Configuration
    @Import(Dispatcher.class)
    static class Config {
        @Bean
        public CircularHandler circularHandler(Dispatcher dispatcher) {
            return new CircularHandler(dispatcher);
        }
    }

    public record CircularRequest() implements Request<CircularResult> {}
    public record CircularResult(boolean success) {}

    @Component
    public static class CircularHandler implements TypedHandler<CircularRequest, CircularResult> {
        private final Dispatcher dispatcher;

        public CircularHandler(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public CircularResult handle(CircularRequest request) {
            // In a real scenario, this might call another command via the dispatcher
            assertNotNull(dispatcher);
            return new CircularResult(true);
        }
    }
}
