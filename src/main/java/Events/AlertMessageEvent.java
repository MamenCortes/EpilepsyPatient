package Events;

/**
 * Event used to notify the UI layer that an alert or warning message
 * should be displayed to the user. This event is typically posted by
 * background services or controllers when an operation fails, a device
 * disconnects, or when user attention is required.
 *
 * <p>
 * The event carries a plain text message that the UI should present in an
 * appropriate alert dialog, toast, or notification component depending on
 * the application's design.
 * </p>
 *
 * <p>
 * This event is intentionally lightweight and contains only the message.
 * Additional metadata such as timestamps, severity levels, or error codes
 * may be added in the future if necessary.
 * </p>
 *
 * <h3>Example usage:</h3>
 *
 * <pre>
 * // Posting an alert event
 * UIEventBus.BUS.post(new AlertMessageEvent("BITalino connection lost."));
 *
 * // Receiving an alert event
 * @Subscribe
 * public void onAlertMessage(AlertMessageEvent event) {
 *     showAlertDialog(event.getMessage());
 * }
 * </pre>
 *
 * @see Events.UIEventBus
 */
public class AlertMessageEvent {

    /** Text message to be displayed in the alert dialog. */
    private final String message;

    /**
     * Creates a new alert event with the given message.
     *
     * @param message text to show to the user
     */
    public AlertMessageEvent(String message) {
        this.message = message;
    }

    /**
     * Returns the message contained in this alert event.
     *
     * @return alert message text
     */
    public String getMessage() {
        return message;
    }
}
