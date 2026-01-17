package nl.bronsenproject.dispatcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for error conditions in the {@link Dispatcher}, such as missing handlers or null requests.
 */
@SpringJUnitConfig(DispatcherErrorHandlingTest.Config.class)
public class DispatcherErrorHandlingTest {

    @Autowired
    private Dispatcher dispatcher;

    @Test
    void testMissingHandler() {
        UnhandledRequest request = new UnhandledRequest();
        assertThrows(IllegalStateException.class, () -> dispatcher.send(request),
                "No handler registered for request type: " + UnhandledRequest.class.getName());
    }

    @Test
    void testNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> dispatcher.send(null),
                "Request cannot be null");
    }

    @Configuration
    @Import(Dispatcher.class)
    static class Config {
        @Bean
        public ValidHandler validHandler() {
            return new ValidHandler();
        }
    }

    public record UnhandledRequest() implements Request<String> {}
    public record ValidRequest() implements Request<String> {}

    public static class ValidHandler implements TypedHandler<ValidRequest, String> {
        @Override
        public String handle(ValidRequest request) {
            return "ok";
        }
    }
}
