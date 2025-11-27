package BITalino;

import pojos.Patient;
import ui.components.AreYouOkayPopup;
import ui.windows.Application;

import javax.swing.*;

public class RecordingController {

    private final AlarmManager alarmManager;
    private final AreYouOkayPopup view;
    private final Application appMain;
    private final Patient patient;
    private boolean popupOpen = false;

    public RecordingController(AreYouOkayPopup view, Application appMain, Patient patient) {
        this.view = view;
        this.appMain = appMain;
        this.patient = patient;
        this.alarmManager = new AlarmManager();
    }

    public void onAnomalyDetected() {

        if (popupOpen) {
            // ya hay popup → no abrir otro
            return;
        }
        popupOpen = true;
        // 1) Mostrar popup (UI)
        view.showAreYouOkayPopup(() -> {
            alarmManager.patientResponse();
            popupOpen = false; // lo que pasa si pulsa "sí"
        });

        // 2) Lanzar alarma de 1 minuto
        alarmManager.triggerAlarm(new AlarmManager.AlarmCallback() {

            @Override
            public void onPatientDidNotRespond() {
                popupOpen = false;
                appMain.client.sendAlertToAdmin(patient);
            }

            @Override
            public void onPatientResponded() {
                popupOpen = false;
                System.out.println("🟢 Paciente respondió a tiempo");
            }
        });
    }
    public void onRecordingError(String message) {
        System.out.println("❌ Recording error: " + message);


        // 2. Notificar a la UI (sin bloquear hilos)
        SwingUtilities.invokeLater(() -> {
            appMain.showErrorMessage("Recording interrupted:\n" + message);

        });
    }

}
