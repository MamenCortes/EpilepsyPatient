package signalRecording;

import Events.AlertMessageEvent;
import Events.ShowHelpDialogEvent;
import Events.UIEventBus;

public class DetectionManager {

    enum State { IDLE, SUSPECTED, ALERT }
    /// IDLE: User is fine, monitoring normally
    /// SUSPECTED: system suspects a seizure and shows the popup
    /// ALERT: the patient doesn't respond to the popup message, seizure alert is sent.

    private State state = State.IDLE;

    //TIMERS
    private long suspectedStart = 0; //When suspicious pattern first appeared
    private long popupStart = 0; //When dialog was shown to user
    private long lastAlert = 0; //When last alert was sent (avoids spamming)

    private static final long CONFIRM_MS = 10000;     // suspicious activity must persist for >10s before showing a popup
    private static final long POPUP_TIMEOUT_MS = 20000; //if the popup is unanswered for >20s → send alert
    private static final long REFRACTORY_MS = 5 * 60 * 1000; //after an alert, ignore detections for 5 minutes



    /// Called every time you receive new data, tipically every few milliseconds or every frame
    /// This method decides if we:
    /// - Stay in IDLE
    /// - Enter SUSTPECTED (show popup)
    /// - Enter ALERT (send server alert)
    public void update(long now, double hr, boolean hrRising, MovementState mv) {

        //A suspicious event happens when the accelerometer shows abnormal movements and the heart rate is rising rapidly or above a fixed threshold
        //This matches medical observations before nocturnal seizures
        boolean suspicious = (mv == MovementState.ABNORMAL) &&
                (hrRising || hr > 110);

        switch (state) {

            case IDLE:
                if (suspicious) {
                    //start timer suspectedStart the first time the suspicious pattern appears
                    if (suspectedStart == 0) suspectedStart = now;
                    //IS suspicion persists for more than 10 seconds AND we are not in the refractory period after ALERT
                    //Then enter the SUSPECTED state and trigger the popup
                    if ((now - suspectedStart) > CONFIRM_MS &&
                            (now - lastAlert) > REFRACTORY_MS) {

                        enterSuspected(now);
                    }
                } else {
                    //If signal returns to normal, reset the timer
                    suspectedStart = 0;
                }
                break;

            case SUSPECTED:
                //popup has been shown, waiting for response
                //This does not block the processing thread, everything continues sampling normally
                if (now - popupStart > POPUP_TIMEOUT_MS) {
                    enterAlert(now);
                }
                break;

            case ALERT:
                //For 5 minutes after sending the alert, ignore new suspicious data
                //Prevents sending multiple alerts in a row
                //After 5 seconds, go back to IDLE
                if (now - lastAlert > REFRACTORY_MS) {
                    state = State.IDLE;
                }
                break;
        }
    }

    private void enterSuspected(long now) {
        state = State.SUSPECTED;
        popupStart = now;

        // Send an evento to the UI thread to show the popup
        UIEventBus.BUS.post(new ShowHelpDialogEvent("Suspected of seizure"));
    }

    private void enterAlert(long now) {
        state = State.ALERT;
        lastAlert = now;

        // Send message to server
        UIEventBus.BUS.post(new AlertMessageEvent("Seizure Detected"));

    }

    // Called when UI user presses “No, I'm fine”
    public void onUserOk() {
        state = State.IDLE;
        suspectedStart = 0;
    }
}
