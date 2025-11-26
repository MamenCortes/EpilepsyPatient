package BITalino;
import java.util.concurrent.*;

public class AlarmManager {

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private volatile boolean patientResponded = false;
    private volatile boolean alarmRunning = false;

    public interface AlarmCallback {
        void onPatientDidNotRespond();   // enviar mensaje al admin
        void onPatientResponded();       // cancelar alerta
    }

    public void triggerAlarm(AlarmCallback callback) {
        if (alarmRunning) {
            System.out.println("⚠ Ya hay una alarma activa. Ignorando…");
            return;
        }

        alarmRunning = true;
        patientResponded = false;

        System.out.println("🚨 Preguntando al paciente si está bien…");

        // Aquí le envías algún mensaje en tu UI o servidor
        sendAlertToPatient();

        // Crear temporizador de 1 minuto
        scheduler.schedule(() -> {
            if (!patientResponded) {
                System.out.println("⏱ Tiempo agotado: no hay respuesta del paciente.");
                callback.onPatientDidNotRespond();
            }
            alarmRunning = false;
        }, 1, TimeUnit.MINUTES);
    }

    public void patientResponse() {
        if (!alarmRunning) return;

        patientResponded = true;
        alarmRunning = false;

        System.out.println("💚 El paciente respondió a tiempo.");

        // Aquí puedes actualizar UI, log, etc.
    }

    private void sendAlertToPatient() {
        System.out.println("📩 Enviando mensaje al paciente: '¿Estás bien?'");
        // Aquí llamas a tu server o UI.
    }
}
