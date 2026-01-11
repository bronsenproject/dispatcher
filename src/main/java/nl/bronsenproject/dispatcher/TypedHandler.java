package nl.bronsenproject.dispatcher;

/**
 * A handler that specifies the exact request type it handles.
 * The Dispatcher uses the generic type information of this interface to route requests.
 *
 * @param <Q> The specific type of request being handled
 * @param <R> The type of the result returned by this handler
 */
public interface TypedHandler<Q extends Request<R>, R> extends Handler<Q, R> {
}
