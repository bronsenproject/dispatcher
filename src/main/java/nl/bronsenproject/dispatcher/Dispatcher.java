package nl.bronsenproject.dispatcher;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The central entry point for dispatching requests to their respective handlers.
 * It automatically discovers all {@link TypedHandler} beans in the Spring context
 * and manages a pipeline of {@link Behavior}s that surround the handler execution.
 */
@Component
public class Dispatcher {

    private final ApplicationContext context;
    private final AtomicReference<Map<Class<?>, TypedHandler<?, ?>>> handlerMap = new AtomicReference<>();
    private final List<Behavior> behaviors;

    /**
     * Initializes a new Dispatcher.
     *
     * @param context   The Spring ApplicationContext used for handler discovery
     * @param behaviors A list of behaviors to be applied to the request pipeline.
     *                  They are typically injected by Spring and sorted by {@code @Order}.
     */
    public Dispatcher(ApplicationContext context, List<Behavior> behaviors) {
        this.context = context;
        // Behaviors are injected sorted by @Order if present
        this.behaviors = behaviors != null ? behaviors : Collections.emptyList();
    }

    /**
     * Provides a thread-safe way to access the discovered handler map.
     * Performs lazy initialization on the first call.
     *
     * @return A map of request classes to their corresponding handlers
     */
    private Map<Class<?>, TypedHandler<?, ?>> getHandlerMap() {
        Map<Class<?>, TypedHandler<?, ?>> map = handlerMap.get();
        if (map == null) {
            map = discoverHandlers();
            if (!handlerMap.compareAndSet(null, map)) {
                map = handlerMap.get();
            }
        }
        return map;
    }

    /**
     * Scans the application context for {@link TypedHandler} beans and maps them to their request types.
     *
     * @return An unmodifiable map of request types to handlers
     * @throws IllegalStateException if generic types cannot be resolved or if multiple handlers exist for the same request
     */
    @SuppressWarnings("rawtypes")
    private Map<Class<?>, TypedHandler<?, ?>> discoverHandlers() {
        // Discover and cache handlers
        Map<String, TypedHandler> beans = context.getBeansOfType(TypedHandler.class);
        Map<Class<?>, TypedHandler<?, ?>> tempMap = new ConcurrentHashMap<>();

        for (TypedHandler handler : beans.values()) {
            Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(handler.getClass(), TypedHandler.class);
            if (typeArgs == null || typeArgs.length == 0) {
                throw new IllegalStateException("Could not resolve generic types for handler: " + handler.getClass().getName());
            }
            Class<?> requestType = typeArgs[0];

            if (tempMap.containsKey(requestType)) {
                throw new IllegalStateException(
                        "Multiple handlers registered for request type: " + requestType.getName());
            }
            tempMap.put(requestType, handler);
        }
        return Collections.unmodifiableMap(tempMap);
    }

    /**
     * Dispatches a request to its handler synchronously, passing it through the registered behaviors.
     *
     * @param request The request to process
     * @param <R>     The expected result type
     * @return The result of the request processing
     * @throws IllegalArgumentException if the request is null
     * @throws IllegalStateException    if no handler is registered for the request type
     */
    @SuppressWarnings("unchecked")
    public <R> R send(Request<R> request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        TypedHandler<Request<R>, R> handler = (TypedHandler<Request<R>, R>) getHandlerMap().get(request.getClass());

        if (handler == null) {
            throw new IllegalStateException("No handler registered for request type: " + request.getClass().getName());
        }

        // Create the execution chain
        Behavior.Next<R> pipeline = () -> handler.handle(request);

        // Wrap behaviors in reverse order so the first behavior in the list is executed first
        for (int i = behaviors.size() - 1; i >= 0; i--) {
            Behavior behavior = behaviors.get(i);
            Behavior.Next<R> next = pipeline;
            pipeline = () -> behavior.handle(request, next);
        }

        return pipeline.call();
    }

    /**
     * Dispatches a request to its handler asynchronously using {@link CompletableFuture#supplyAsync(java.util.function.Supplier)}.
     *
     * @param request The request to process
     * @param <R>     The expected result type
     * @return A CompletableFuture that will contain the result of the request processing
     */
    public <R> CompletableFuture<R> sendAsync(Request<R> request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }
}
