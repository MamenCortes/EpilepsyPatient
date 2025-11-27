package BITalino;

import java.util.ArrayDeque;
import java.util.Deque; // una cola a la que puedes añadir y quitar por ambos extremos
//más eficiente que un ArrayList para estructuras tipo ventana deslizante
// viene en el package


public class ECGRealTimeAnalyzer {

    private final double samplingRate;
    private final int windowSize;     // p.ej. 200 ms
    private final Deque<Double> window;
    private final AlarmManager alarmManager;
    private double maxValueInWindow = 0;

    private int lastPeakSample = -1;
    private int currentSample = 0;

    public interface HRListener {
        void onHeartRate(double hr);
        void onPeakDetected(int sampleIndex);
        void onBradycardia(double hr);
        void onTachycardia(double hr);
    }
    private final HRListener listener;

    public ECGRealTimeAnalyzer(double samplingRate, HRListener listener, AlarmManager alarmManager) {
        this.samplingRate = samplingRate;
        this.listener = listener;
        this.alarmManager = alarmManager;
        this.windowSize = (int)(0.2 * samplingRate);
        this.window = new ArrayDeque<>();
    }

    public boolean addSample(double sampleValue) {
        boolean anomalyDetected = false;
        window.addLast(sampleValue);
        if (window.size() > windowSize) {
            double removed = window.removeFirst();
            if (removed == maxValueInWindow) {
                // recalcular máximo si hemos eliminado el máximo anterior
                maxValueInWindow = window.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            }
        }

        // actualizar máximo
        if (Math.abs(sampleValue) > maxValueInWindow) {
            maxValueInWindow = Math.abs(sampleValue);
        }

        // threshold adaptativo del 50% del máximo reciente
        double threshold = 0.8 * maxValueInWindow;

        // detectar R-peak
        boolean isPeak = Math.abs(sampleValue) > threshold;

        if (isPeak && (currentSample - lastPeakSample) > (0.6* samplingRate)) {
            // evita múltiples detecciones del mismo pico

            // notificar pico
            listener.onPeakDetected(currentSample);

            if (lastPeakSample != -1) {
                double rr_sec = (currentSample - lastPeakSample) / samplingRate;
                double hr = 60.0 / rr_sec;

                listener.onHeartRate(hr);

                if (hr < 50) {
                    anomalyDetected= true;

                    listener.onBradycardia(hr);
                    alarmManager.triggerAlarm(new AlarmManager.AlarmCallback() {
                        @Override
                        public void onPatientDidNotRespond() {
                            listener.onBradycardia(hr);
                            // feedback a la UI si quieres
                        }
                        @Override
                        public void onPatientResponded() {
                            System.out.println("🟢 El paciente está bien.");
                        }
                    });
                }
                if (hr > 100){
                    anomalyDetected= true;
                    listener.onTachycardia(hr);

                    alarmManager.triggerAlarm(new AlarmManager.AlarmCallback() {
                        @Override
                        public void onPatientDidNotRespond() {
                            listener.onTachycardia(hr);
                        }
                        @Override
                        public void onPatientResponded() {
                            System.out.println("🟢 El paciente está bien.");
                        }
                    });
                }
            }

            lastPeakSample = currentSample;
        }

        currentSample++;
        return anomalyDetected;
    }

}
