package signalRecording;
/**
 * Exception class used to represent errors that occur during the recording
 * or processing of physiological signals (ECG, ACC, etc.).
 * <p>
 * This exception wraps a {@link RecordingErrors} value, which provides:
 * <ul>
 *     <li>A specific error code describing what failed</li>
 *     <li>A human-readable message via {@code getFullMessage()}</li>
 * </ul>
 * The full message from the associated {@code RecordingErrors} enum is passed
 * to the superclass {@link Exception} so that stack traces and logs contain
 * meaningful diagnostic information.
 */
public class RecordingException extends Exception {

  private final RecordingErrors error;
  /**
   * Creates a new {@code RecordingException} using the provided error type.
   * The error's full descriptive message is automatically propagated to the
   * base {@link Exception} class.
   *
   * @param error The specific {@link RecordingErrors} associated with this failure.
   */
  public RecordingException(RecordingErrors error) {
    super(error.getFullMessage());
    this.error = error;
  }
  /**
   * Returns the specific {@link RecordingErrors} enumerator that triggered this exception.
   *
   * @return The associated recording error type.
   */

  public RecordingErrors getError() {
    return error;
  }
}