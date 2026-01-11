package nl.bronsenproject.dispatcher;

/**
 * A marker interface for read-only operations.
 * Queries are intended to represent requests for data that do not change the system state.
 *
 * @param <R> The type of the result returned after processing this query
 */
public interface Query<R> extends Request<R> {
}
