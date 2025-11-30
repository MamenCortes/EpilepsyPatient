package signalRecording;
/**
 * Enumeration of all possible error conditions that can occur inside
 * the SignalRecorderService during BITalino connection, acquisition,
 * processing or file generation.
 */
public enum RecordingErrors {

    // ---------------- CONNECTION ERRORS ----------------

    INVALID_MAC_ADDRESS(
            "Invalid MAC address",
            "The provided MAC address is null, empty or malformed."
    ),

    CONNECTION_FAILED(
            "Connection failed",
            "Could not establish Bluetooth connection with the BITalino device."
    ),

    DEVICE_NOT_CONNECTED(
            "Device not connected",
            "BITalino is not connected when an operation required an active connection."
    ),

    LOST_COMMUNICATION(
            "Lost communication",
            "The Bluetooth link with BITalino was unexpectedly interrupted."
    ),

    INVALID_SAMPLING_FREQUENCY(
            "Invalid sampling frequency",
            "The sampling frequency is out of supported range or rejected by BITalino."
    ),

    INVALID_CHANNEL_CONFIGURATION(
            "Invalid channel configuration",
            "The channel setup for recording is invalid or unsupported."
    ),


    // ---------------- RECORDING ERRORS ----------------

    ALREADY_RECORDING(
            "Already recording",
            "Recording was started while another recording session is still running."
    ),

    STOPPED_WHEN_NOT_RECORDING(
            "Cannot stop recording",
            "Attempted to stop a recording when no acquisition is running."
    ),

    RECORDING_INTERRUPTED(
            "Recording interrupted",
            "Recording stopped unexpectedly due to a hardware disconnection."
    ),

    DEVICE_READ_ERROR(
            "Device read error",
            "BITalino threw an exception while reading frames."
    ),

    READ_THREAD_CRASHED(
            "Read thread crashed",
            "ReadThread terminated unexpectedly due to an exception."
    ),

    ANALYZE_THREAD_CRASHED(
            "Analyze thread crashed",
            "AnalyzeThread encountered an unexpected exception."
    ),

    SAVE_THREAD_CRASHED(
            "Save thread crashed",
            "SaveThread encountered an error while writing the CSV file."
    ),


    // ---------------- FILE / ZIP ERRORS ----------------

    CSV_CREATION_FAILED(
            "CSV creation failed",
            "The temporary CSV file could not be created."
    ),

    CSV_WRITE_ERROR(
            "CSV write error",
            "Writing data into the CSV file failed unexpectedly."
    ),

    CSV_NOT_FOUND(
            "CSV missing",
            "CSV file was not generated or is empty when attempting to create ZIP."
    ),

    ZIP_CREATION_FAILED(
            "ZIP creation failed",
            "Could not compress the CSV file into a ZIP archive."
    ),

    ZIP_IO_ERROR(
            "ZIP I/O error",
            "An I/O error occurred during ZIP creation."
    ),


    // ---------------- INTERNAL ERRORS ----------------

    QUEUE_PUT_ERROR(
            "Queue error",
            "Could not insert an element into one of the internal BlockingQueues."
    ),

    POISON_PILL_FAILURE(
            "Poison pill error",
            "Poison pill was not delivered correctly to processing threads."
    ),

    ILLEGAL_INTERNAL_STATE(
            "Illegal internal state",
            "The service entered a state inconsistent with expected recording logic."
    ),

    UNEXPECTED_EXCEPTION(
            "Unexpected exception",
            "An unhandled internal Java exception occurred."
    );



    // -----------------------------------------------
    // FIELDS & CONSTRUCTOR
    // -----------------------------------------------

    private final String title;
    private final String description;

    RecordingErrors(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * @return short human-readable name of the error.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return detailed explanation of the error cause.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return full error text for logging/debugging.
     */
    public String getFullMessage() {
        return title + ": " + description;
    }
}
