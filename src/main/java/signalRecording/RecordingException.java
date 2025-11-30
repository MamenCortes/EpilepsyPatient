package signalRecording;
/**
 * Exception representing an error during signal recording.
 * Wraps a {@link RecordingErrors} enum value with detailed information.
 */
public class RecordingException extends Exception {

  private final RecordingErrors error;
    /**
     * Creates a new RecordingException using a specific RecordingErrors value.
     *
     * @param error the error type encountered during recording
     */
  public RecordingException(RecordingErrors error) {
    super(error.getFullMessage());
    this.error = error;
  }
    /**
     * Returns the underlying error type.
     *
     * @return the RecordingErrors instance associated to this exception
     */
  public RecordingErrors getError() {
    return error;
  }
}