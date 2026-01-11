package nl.bronsenproject.dispatcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(DispatcherBasicTest.Config.class)
public class DispatcherBasicTest {

    @Autowired
    private Dispatcher dispatcher;

    @Test
    void testSendSynchronous() {
        PingRequest request = new PingRequest("Ping");
        PongResponse response = dispatcher.send(request);
        assertNotNull(response);
        assertEquals("Pong: Ping", response.message());
    }

    @Test
    void testSendAsynchronous() throws ExecutionException, InterruptedException {
        PingRequest request = new PingRequest("PingAsync");
        CompletableFuture<PongResponse> future = dispatcher.sendAsync(request);
        assertNotNull(future);
        PongResponse response = future.get();
        assertEquals("Pong: PingAsync", response.message());
    }

    @Test
    void testCommandMarker() {
        CreateCommand command = new CreateCommand("Item");
        String result = dispatcher.send(command);
        assertEquals("Created: Item", result);
    }

    @Test
    void testQueryMarker() {
        GetQuery query = new GetQuery(123L);
        String result = dispatcher.send(query);
        assertEquals("Result for: 123", result);
    }

    @Configuration
    @Import(Dispatcher.class)
    static class Config {
        @Bean
        public PingHandler pingHandler() {
            return new PingHandler();
        }

        @Bean
        public CreateHandler createHandler() {
            return new CreateHandler();
        }

        @Bean
        public GetHandler getHandler() {
            return new GetHandler();
        }
    }

    public record PingRequest(String message) implements Request<PongResponse> {}
    public record PongResponse(String message) {}

    public static class PingHandler implements TypedHandler<PingRequest, PongResponse> {
        @Override
        public PongResponse handle(PingRequest request) {
            return new PongResponse("Pong: " + request.message());
        }
    }

    public record CreateCommand(String name) implements Command<String> {}

    public static class CreateHandler implements TypedHandler<CreateCommand, String> {
        @Override
        public String handle(CreateCommand request) {
            return "Created: " + request.name();
        }
    }

    public record GetQuery(Long id) implements Query<String> {}

    public static class GetHandler implements TypedHandler<GetQuery, String> {
        @Override
        public String handle(GetQuery request) {
            return "Result for: " + request.id();
        }
    }
}
