package Events;

/**
 * Event used to request the UI layer to display a help or information dialog.
 * <p>
 * This event is typically posted by background processes or presenters when
 * the application needs to show guidance, warnings, or user instructions
 * without directly coupling the business logic to the graphical components.
 * </p>
 *
 * <p>
 * The event carries a message string that should be shown in a modal or
 * non-modal dialog depending on the UI implementation. Additional fields
 * may be added if future requirements include metadata such as timestamps
 * or contextual identifiers.
 * </p>
 *
 * @see Events.UIEventBus
 */
public class ShowHelpDialogEvent {

    /** Message to be displayed in the help dialog. */
    private final String message;

    /**
     * Creates a new event with the specified message.
     *
     * @param message the text content to display in the help dialog
     */
    public ShowHelpDialogEvent(String message) {
        this.message = message;
    }

    /**
     * Returns the message associated with this event.
     *
     * @return the help dialog message
     */
    public String getMessage() {
        return message;
    }
}