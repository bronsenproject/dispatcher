package nl.bronsenproject.dispatcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(DispatcherBehaviorTest.Config.class)
public class DispatcherBehaviorTest {

    @Autowired
    private Dispatcher dispatcher;

    @Autowired
    private ExecutionLog executionLog;

    @Test
    void testBehaviorsInOrder() {
        executionLog.clear();
        PingRequest request = new PingRequest("ping");
        dispatcher.send(request);

        List<String> expectedLog = List.of(
                "Behavior1: before",
                "Behavior2: before",
                "Handler: handle",
                "Behavior2: after",
                "Behavior1: after"
        );
        assertEquals(expectedLog, executionLog.getLog());
    }

    @Configuration
    @Import(Dispatcher.class)
    static class Config {
        @Bean
        public ExecutionLog executionLog() {
            return new ExecutionLog();
        }

        @Bean
        @Order(1)
        public Behavior behavior1(ExecutionLog log) {
            return new LoggingBehavior("Behavior1", log);
        }

        @Bean
        @Order(2)
        public Behavior behavior2(ExecutionLog log) {
            return new LoggingBehavior("Behavior2", log);
        }

        @Bean
        public PingHandler pingHandler(ExecutionLog log) {
            return new PingHandler(log);
        }
    }

    public record PingRequest(String message) implements Request<String> {}

    public static class PingHandler implements TypedHandler<PingRequest, String> {
        private final ExecutionLog log;

        public PingHandler(ExecutionLog log) {
            this.log = log;
        }

        @Override
        public String handle(PingRequest request) {
            log.add("Handler: handle");
            return "pong";
        }
    }

    public static class LoggingBehavior implements Behavior {
        private final String name;
        private final ExecutionLog log;

        public LoggingBehavior(String name, ExecutionLog log) {
            this.name = name;
            this.log = log;
        }

        @Override
        public <R> R handle(Request<R> request, Next<R> next) {
            log.add(name + ": before");
            R result = next.call();
            log.add(name + ": after");
            return result;
        }
    }

    public static class ExecutionLog {
        private final List<String> log = new ArrayList<>();

        public void add(String message) {
            log.add(message);
        }

        public List<String> getLog() {
            return log;
        }

        public void clear() {
            log.clear();
        }
    }
}
