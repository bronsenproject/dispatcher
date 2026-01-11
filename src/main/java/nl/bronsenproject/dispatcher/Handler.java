package nl.bronsenproject.dispatcher;

/**
 * Handles a specific request and returns a result.
 * This is the base interface for all handlers in the dispatcher system.
 *
 * @param <Q> The type of request being handled
 * @param <R> The type of the result returned by this handler
 */
public interface Handler<Q extends Request<R>, R> {
    /**
     * Processes the given request.
     *
     * @param request The request to handle
     * @return The result of processing the request
     */
    R handle(Q request);
}
