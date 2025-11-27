package Events;

public class ShowHelpDialogEvent {
    private String message;
    // add fields if needed (like HR, timestamp, etc)
    public ShowHelpDialogEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
