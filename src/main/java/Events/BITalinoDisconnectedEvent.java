package Events;

import java.time.LocalDateTime;

/**
 * Event posted to the UI layer when the BITalino device becomes disconnected
 * during an active acquisition session. This event is typically sent from
 * worker threads and contains minimal metadata for UI notification.
 */
public class BITalinoDisconnectedEvent {

    /** Moment when the disconnection was detected. */
    private final LocalDateTime timestamp;

    /** Whether a partial recording was generated (ZIP with incomplete data). */
    private final boolean partialRecordingAvailable;

    /** Optional message describing the reason for disconnection. */
    private final String message;

    /**
     * Creates a disconnection event with standard default values.
     * Use when no additional information is needed.
     */
    public BITalinoDisconnectedEvent() {
        this(LocalDateTime.now(), false, null);
    }

    /**
     * Creates a disconnection event with detailed metadata.
     *
     * @param timestamp                 time of disconnection detection
     * @param partialRecordingAvailable true if a partial ZIP was created
     * @param message                   optional descriptive message
     */
    public BITalinoDisconnectedEvent(LocalDateTime timestamp,
                                     boolean partialRecordingAvailable,
                                     String message) {
        this.timestamp = timestamp;
        this.partialRecordingAvailable = partialRecordingAvailable;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isPartialRecordingAvailable() {
        return partialRecordingAvailable;
    }

    public String getMessage() {
        return message;
    }
}
