package Events;

public class AlertMessageEvent {
    private String message;

    public AlertMessageEvent(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
