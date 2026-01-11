package nl.bronsenproject.dispatcher;

/**
 * Defines a cross-cutting concern in the request processing pipeline.
 * Common uses include logging, transaction management, validation, or metrics.
 * Implementing beans are automatically discovered and integrated into the {@link Dispatcher} pipeline.
 * Use {@code @org.springframework.core.annotation.Order} to control the execution order of behaviors.
 */
public interface Behavior {
    /**
     * Handles the request and decides whether to proceed to the next step in the pipeline.
     *
     * @param <R>     The expected result type
     * @param request The request being processed
     * @param next    The next step in the pipeline (could be another behavior or the final handler)
     * @return The result of the operation
     */
    <R> R handle(Request<R> request, Next<R> next);

    /**
     * Represents the next operation in the pipeline.
     * Calling {@link #call()} will execute the next behavior or the final handler.
     *
     * @param <R> The type of the result
     */
    @FunctionalInterface
    interface Next<R> {
        /**
         * Continues the execution of the pipeline.
         *
         * @return The result from the subsequent steps in the pipeline
         */
        R call();
    }
}
