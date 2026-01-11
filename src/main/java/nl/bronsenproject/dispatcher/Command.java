package nl.bronsenproject.dispatcher;

/**
 * A marker interface for write operations (state changes).
 * Commands are intended to represent actions that change the state of the system.
 *
 * @param <R> The type of the result returned after processing this command
 */
public interface Command<R> extends Request<R> {
}
