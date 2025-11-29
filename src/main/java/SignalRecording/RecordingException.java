package SignalRecording;

public class RecordingException extends Exception {

  private final RecordingErrors error;

  public RecordingException(RecordingErrors error) {
    super(error.getFullMessage());
    this.error = error;
  }

  public RecordingErrors getError() {
    return error;
  }
}