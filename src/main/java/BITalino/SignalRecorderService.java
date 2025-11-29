package BITalino;

import Events.BITalinoDisconnectedEvent;
import Events.UIEventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.Client;
import org.junit.Test;
import pojos.Signal;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class SignalRecorderService {

    private static String MAC_ADDRESS;

    private BITalino bitalino;
    private volatile boolean isRecording = false;
    private volatile boolean connected = false;
    private volatile boolean recordingInterrupted = false;

    private final int fs=100; // sampling frequency
    private Thread readThread;
    private Thread analyzeThread;
    private Thread saveThread;

    private File csvTempFile;
    private File zipFile;

    // Colas para comunicación entre hilos
    private final BlockingQueue<Frame> frameQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Frame> saveQueue = new LinkedBlockingQueue<>();

    // Poison pill para indicar fin de datos a Analyze y Save
    private static final Frame POISON_PILL = new Frame();

    // Procesadores de señal y detector de eventos
    private final EcgProcessor ecgProcessor=new EcgProcessor();
    private final AccProcessor accProcessor=new AccProcessor();
    private final DetectionManager detectionManager=new DetectionManager();


    public SignalRecorderService(String MAC_ADDRESS) {
        SignalRecorderService.MAC_ADDRESS = MAC_ADDRESS;
    }
    public int getFs() {
        return fs;
    }
    public void bitalinoConnect() {
        try {
            System.out.println("🔌 Conectando al BITalino...");
            bitalino = new BITalino();
            bitalino.open(MAC_ADDRESS, fs);
            connected = true;
            System.out.println("✅ Conexión establecida.");
        } catch (Exception e) {
            System.out.println("❌ Error al conectar con BITalino en " + MAC_ADDRESS);
        }
    }

    public void startRecording() throws BITalinoException {
        try {
            if (!connected || bitalino == null) {
                throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
            }
            int[] channelsToRead = {1,4}; // ECG en A2 y Acelerómetro en A1
            bitalino.start(channelsToRead);

            isRecording = true;
            recordingInterrupted = false;

            readThread = new Thread(new ReadThread());
            analyzeThread = new Thread(new AnalyzeThread());
            saveThread = new Thread(new SaveThread());

            readThread.start();
            analyzeThread.start();
            saveThread.start();

            System.out.println("🎯 Hilos en ejecución (Read / Analyze / Save)");

        } catch (Throwable e) {
            e.printStackTrace();
            throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);

        }
    }

    public void stopRecording()throws BITalinoException {
        if (!isRecording && !recordingInterrupted) {
            System.out.println("⚠ stopRecording() llamado pero ya no se estaba grabando.");
            return;
        }
        // Si ya se había interrumpido por desconexión, no vuelvas a hacer nada gordo
        if (recordingInterrupted) {
            System.out.println("⚠ stopRecording() llamado tras desconexión. No se hace nada.");
            return;
        }

        isRecording = false;
        System.out.println("🛑 Deteniendo adquisición...");

        try {frameQueue.put(POISON_PILL);} catch (InterruptedException ignored) {}
        try {saveQueue.put(POISON_PILL);} catch (InterruptedException ignored) {}

        // Cerrar BITalino si sigue vivo
        try {
            if (connected && bitalino != null) {
                bitalino.stop();
                bitalino.close();
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally { connected = false; }

        try {
            if (readThread != null) {
                System.out.println("⏳ Esperando a que termine ReadThread...");
                readThread.join();
            }
            if (analyzeThread != null) {
                System.out.println( "⏳ Esperando a que termine AnalyzeThread...");
                analyzeThread.join();
            }
            if (saveThread != null) {
                System.out.println("⏳ Esperando a que termine SaveThread...");
                saveThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (csvTempFile != null && csvTempFile.exists()) {
            zipFile = compressToZip(csvTempFile);
            if (zipFile != null) {
                System.out.println("📦 ZIP creado en: " + zipFile.getAbsolutePath());
            } else {
                System.out.println("⚠ No se pudo crear el ZIP.");
            }
        } else {
            System.out.println("⚠ No hay CSV para comprimir.");
        }
        UIEventBus.BUS.post(new BITalinoDisconnectedEvent());
        System.out.println("Recording stopped");
    }

    public File getZipFile() {
        return zipFile;
    }
    public boolean isConnected() {
        return connected;
    }


    // ------------------------ THREADS ------------------------

    private class ReadThread implements Runnable {
        @Override
        public void run() {
            System.out.println("🟢 [ReadThread] Iniciado");
            try {
                while (isRecording) {
                    Frame[] frames = bitalino.read(10);
                    if (frames == null) continue;
                    for (Frame f : frames) {
                        frameQueue.put(f);
                        saveQueue.put(f);
                    }
                }
            } catch (BITalinoException | InterruptedException e) {
                if (isRecording) {
                    stopAfterDisconnection();
                    System.out.println("❌ BITalinoException en ReadThread: " + e.getMessage());
                    recordingInterrupted = true;
                    isRecording = false;
                    connected = false;

                    // Enviar poison pill para que Analyze y Save terminen
                    try {
                        frameQueue.put(POISON_PILL);
                    } catch (InterruptedException ignored) {}
                    try {
                        saveQueue.put(POISON_PILL);
                    } catch (InterruptedException ignored) {}

                } else {
                    System.out.println("🔴 [ReadThread] BITalinoException tras parar la grabación.");
                }
            }

            System.out.println("🔴 [ReadThread] Finalizado");
        }
    }


    private void stopAfterDisconnection() {
        System.out.println("🛑 Parada por desconexión de BITalino...");

        isRecording = false;
        recordingInterrupted = true;
        connected = false;

        // Enviar poison pill para que terminen Analyze y Save
        try { frameQueue.put(POISON_PILL); } catch (InterruptedException ignored) {}
        try { saveQueue.put(POISON_PILL); } catch (InterruptedException ignored) {}

        // Intentar cerrar BITalino, pero si ya está muerto, no pasa nada
        try {
            if (bitalino != null) {
                bitalino.stop();
                bitalino.close();
            }
        } catch (Exception e) {
            System.out.println("⚠ Error cerrando BITalino tras desconexión (ya estaría muerto): " + e.getMessage());
        }

        // Esperar SOLO a Analyze y Save (NO a Read, que es este hilo)
        try {
            if (analyzeThread != null) analyzeThread.join();
            if (saveThread != null)    saveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Crear ZIP con lo que haya
        if (csvTempFile != null && csvTempFile.exists()) {
            zipFile = compressToZip(csvTempFile);
            if (zipFile != null) {
                System.out.println("📦 ZIP parcial creado tras desconexión: " + zipFile.getAbsolutePath());
                UIEventBus.BUS.post(new BITalinoDisconnectedEvent());

            }
        } else {
            System.out.println("⚠ No hay CSV para comprimir tras desconexión.");
        }
    }

    private class AnalyzeThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Frame f = frameQueue.take();
                    if (f == POISON_PILL) {
                        System.out.println("🟣 [AnalyzeThread] Poison pill recibido. Terminando.");
                        break;
                    }
                    analyzeSignals(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("🟣 [AnalyzeThread] Finalizado");

        }



    }

    //TODO: revisar
    //Alternative analyzeDignal function implementing the ecgProcessor and accProcessor
    private void analyzeSignals(Frame f) {
        long ts = System.currentTimeMillis();

        double ecg = f.analog[0]; // ECG en A2
        double acc= f.analog[1];
       // System.out.println("ECG: " + ecg + " | ACC: " + acc);
        ecgProcessor.addSample(ecg, ts);
        accProcessor.addSample(acc, ts);

        double hr = ecgProcessor.getCurrentHeartRate();
        boolean hrRising = ecgProcessor.isHeartRateRising();
        MovementState movement = accProcessor.getMovementState();

        detectionManager.update(ts, hr, hrRising, movement);
    }


    private class SaveThread implements Runnable {
        @Override
        public void run() {
            System.out.println("💾 [SaveThread] Iniciado");

            try {
                csvTempFile = File.createTempFile("bitalino_", ".csv");
                csvTempFile.deleteOnExit();
                System.out.println("💾 [SaveThread] CSV en: " + csvTempFile.getAbsolutePath());

                try (FileWriter writer = new FileWriter(csvTempFile)) {
                    while (true) {
                        Frame f = saveQueue.take();
                        if (f == POISON_PILL) {
                            System.out.println("💾 [SaveThread] Poison pill recibido. Terminando.");
                            break;
                        }

                        // ⚠ Ajusta el orden según tus canales
                        writer.write(
                                f.analog[0] + ";" + // por ejemplo ECG
                                        (f.analog.length > 1 ? f.analog[1] : 0) + "\n"
                        );
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }

            System.out.println("🟦 [SaveThread] Finalizado");
        }
    }

    // ---------------- ZIP ------------------------

    public File compressToZip(File csvFile) {
        try {
            File zip = File.createTempFile("bitalino_", ".zip");
            zip.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(zip);
                 java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);
                 FileInputStream fis = new FileInputStream(csvFile)) {

                zos.putNextEntry(new java.util.zip.ZipEntry(csvFile.getName()));

                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
            }

            return zip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testSaveThreadCreatesCSV() throws Exception {
        SignalRecorderService service = new SignalRecorderService("00:00:00:00:00:00");

        // Arrancamos solo el SaveThread
        Thread saveThread = new Thread(service.new SaveThread());
        service.isRecording = true;
        saveThread.start();

        // Injectamos frames de prueba
        Frame f = new Frame();
        f.analog = new int[] {100, 200, 300, 400};
        service.saveQueue.put(f);

        // Parar la grabación para que SaveThread termine
        service.isRecording = false;
        saveThread.join();

        File csv = service.csvTempFile;
        assertTrue(csv.exists());
        assertTrue(csv.length() > 0);

        // Leer contenido
        String line = new BufferedReader(new FileReader(csv)).readLine();
        assertEquals("100;200;300;400", line);
        System.out.println(csv.getAbsolutePath());
    }
    @Test
    public void testCompressToZip() throws Exception {
        SignalRecorderService service = new SignalRecorderService("00:00:00:00:00:00");

        // Crear archivo temporal simulado
        File temp = File.createTempFile("test_", ".csv");
        try(FileWriter writer = new FileWriter(temp)) {
            writer.write("data");
        }

        File zip = service.compressToZip(temp);
        assertTrue(zip.exists());
        assertTrue(zip.length() > 0);

        // Verificar contenido ZIP
        ZipFile zipFile = new ZipFile(zip);
        assertEquals(1, zipFile.size());
        assertNotNull(zipFile.getEntry(temp.getName()));
        System.out.println(zip.getAbsolutePath());

    }


}

