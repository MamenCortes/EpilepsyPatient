package Events;
import com.google.common.eventbus.EventBus;

/**
 * Global event bus used by the UI layer to receive and dispatch application
 * events in a decoupled manner. This class wraps a singleton instance of
 * Google's {@link com.google.common.eventbus.EventBus}, providing a central
 * communication hub between background services, threads, and user interface
 * components.
 *
 * <p>
 * The {@code UIEventBus} allows different parts of the application to publish
 * events (e.g. device disconnection, help dialog requests, status updates)
 * without creating direct dependencies between components. Subscribers in
 * the UI register to the bus to receive notifications asynchronously.
 * </p>
 *
 * <p>
 * This class cannot be instantiated. It exposes a single static {@code BUS}
 * instance that should be shared across the entire application.
 * </p>
 *
 * <h3>Example usage:</h3>
 *
 * <pre>
 * // Posting an event
 * UIEventBus.BUS.post(new ShowHelpDialogEvent("BITalino disconnected."));
 *
 * // Subscribing to an event
 * @Subscribe
 * public void handleHelpDialog(ShowHelpDialogEvent event) {
 *     showDialog(event.getMessage());
 * }
 * </pre>
 *
 * @see com.google.common.eventbus.EventBus
 * @see Events.ShowHelpDialogEvent
 * @see Events.BITalinoDisconnectedEvent
 * @see Events.ServerDisconnectedEvent
 */
public class UIEventBus {

    /** Global singleton event bus for UI communication. */
    public static final EventBus BUS = new EventBus();

    /** Prevents instantiation of this utility class. */
    private UIEventBus() {}
}
