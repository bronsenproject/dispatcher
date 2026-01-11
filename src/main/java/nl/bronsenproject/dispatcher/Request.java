package nl.bronsenproject.dispatcher;

/**
 * Marker interface for a request that returns a result of type R.
 * Implementations of this interface represent a message that can be dispatched to a handler.
 *
 * @param <R> The type of the result returned after processing this request
 */
public interface Request<R> {
}
