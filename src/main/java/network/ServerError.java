package network;
/**
 * Exception thrown when the server responds with an error status.
 */
public class ServerError extends RuntimeException {
    /**
     * Creates a new ServerError with the given message.
     *
     * @param message the server error message
     */
    public ServerError(String message) {
        super(message);
    }
}
