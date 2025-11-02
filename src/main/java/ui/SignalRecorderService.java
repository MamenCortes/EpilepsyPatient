package ui;

import ceu.biolab.BITalino.BITalino;
import ceu.biolab.BITalino.Frame;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SignalRecorderService {

    private static final String MAC_ADDRESS = "98:D3:C1:FD:2F:EA";

    private BITalino bitalino;
    private volatile boolean isRecording = false;

    // Cola compartida entre hilos (almacena los frames recién leídos)
    //crea una cola segura para pasar frames del BITalino del hilo de lectura al hilo de análisis,
    //sin necesidad de sincronizar manualmente los threads ni usar wait()/notify().
    private final BlockingQueue<Frame> frameQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> sendQueue = new LinkedBlockingQueue<>();
// modificar el código para enviar los datos analizados a través de la red

    // === MÉTODO PRINCIPAL ===
    public void startRecording() {
        try {
            System.out.println("🔌 Conectando al BITalino...");
            bitalino = new BITalino();
            bitalino.open(MAC_ADDRESS, 1000); // frecuencia de muestreo
            System.out.println("✅ Conexión establecida.");

            int[] channelsToRead = {1, 2, 3, 4};
            bitalino.start(channelsToRead);
            System.out.println("▶️ Adquisición iniciada.");

            isRecording = true;

            // --- Hilo 1: lectura de frames ---
            Thread readThread = new Thread(new ReadThread());
            // --- Hilo 2: análisis de frames ---
            Thread analyzeThread = new Thread(new AnalyzeThread());

            readThread.start();
            analyzeThread.start();

            System.out.println("🎯 Hilos de lectura y análisis en ejecución...");

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        isRecording = false;
        System.out.println("🛑 Deteniendo adquisición...");

        try {
            if (bitalino != null) {
                bitalino.stop();
                bitalino.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("✅ BITalino detenido correctamente.");
    }

    // === CLASE INTERNA: LECTOR (como 'Read' en tu ejemplo) ===
    private class ReadThread implements Runnable {
        @Override
        public void run() {
            System.out.println("🟢 [ReadThread] Hilo de lectura iniciado: " + Thread.currentThread().getName());
            try {
                while (isRecording) {
                    Frame[] frames = bitalino.read(10);
                    if (frames == null) {
                        System.out.println("⚠️ [ReadThread] frames == null");
                        continue;
                    }
                    if (frames.length == 0) {
                        System.out.println("⚠️ [ReadThread] frames vacíos");
                        continue;
                    }

                    for (Frame f : frames) {
                        if (f == null) {
                            System.out.println("⚠️ [ReadThread] Frame nulo");
                            continue;
                        }
                        if (f.analog == null) {
                            System.out.println("⚠️ [ReadThread] f.analog == null");
                            continue;
                        }

                        System.out.println("📡 [ReadThread] ECG recibido: " + f.analog[0]);
                        frameQueue.put(f);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("🔴 [ReadThread] Lectura finalizada.");
        }
    }


    // === CLASE INTERNA: ANALIZADOR (como 'Write' en tu ejemplo) ===
    private class AnalyzeThread implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("🟣 [AnalyzeThread] Hilo de análisis iniciado: " + Thread.currentThread().getName());

                while (isRecording || !frameQueue.isEmpty()) {
                    Frame f = frameQueue.take(); // bloquea hasta que haya datos
                    System.out.println("🧠 [AnalyzeThread] Procesando frame ECG=" + f.analog[0]);
                    double ecg = f.analog[0];
                    double ax = f.analog[1];
                    double ay = f.analog[2];
                    double az = f.analog[3];

                    // Guarda en CSV

                    // Analiza la señal
                    analyzeSignals(ecg, ax, ay, az);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // === Método de análisis en tiempo real ===
    private void analyzeSignals(double ecg, double ax, double ay, double az) {
        double accMagnitude = Math.sqrt(ax * ax + ay * ay + az * az);

        if (ecg > 800) {
            System.out.println("⚠️ ECG alto: " + ecg);
        }
        if (accMagnitude > 1200) {
            System.out.println("⚡ Movimiento brusco detectado (" + accMagnitude + ")");
        }
        if (ecg > 900 && accMagnitude > 1200) {
            System.out.println("🚨 Posible ataque (ECG + ACC altos simultáneamente)");
        }
    }
}
