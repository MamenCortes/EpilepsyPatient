package Events;

/**
 * Event posted to the UI layer when the connection with the remote server
 * is lost unexpectedly. This event acts as a marker notification used by
 * the client-side components to update the interface, cancel pending actions,
 * or return the user to a safe screen.
 * <p>
 * Typical scenarios triggering this event include:
 * <ul>
 *     <li>Server shutdown or restart</li>
 *     <li>Network failure or connectivity loss</li>
 *     <li>Unexpected socket closure</li>
 *     <li>Protocol violation or broken communication channel</li>
 * </ul>
 *
 * <p>
 * The event intentionally contains no payload. Its meaning is purely
 * semantic and is consumed by subscribers registered in the UI EventBus.
 * </p>
 *
 * @see Events.UIEventBus
 */
public class ServerDisconnectedEvent {
}
