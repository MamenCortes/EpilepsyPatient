package BITalino;

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

    private static  String MAC_ADDRESS ;

    private BITalino bitalino;
    private boolean isRecording = false;
    private boolean connected=false;
    private File csvTempFile;
    private File zipFile;
    private RecordingController controller;
    private volatile boolean recordingInterrupted = false;
    private Thread saveThread;
    private ECGRealTimeAnalyzer analyzer;
    private final int fs = 100; // Sampling frequency
    private final BlockingQueue<Frame> frameQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Frame> saveQueue = new LinkedBlockingQueue<>();

    //TODO: eliminar si se decide no implementar así
    private EcgProcessor ecgProcessor = new EcgProcessor();
    private AccProcessor accProcessor = new AccProcessor();
    private DetectionManager detectionManager = new DetectionManager();


    public SignalRecorderService(String MAC_ADDRESS) {
        SignalRecorderService.MAC_ADDRESS = MAC_ADDRESS;
    }

    public void setRecordingContoller (RecordingController controller) {
        this.controller = controller;

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
            e.printStackTrace();
        }
    }

    public void startRecording() {
        try {

            int[] channelsToRead = {1,2}; // ECG en A2 y Acelerómetro en A1
            bitalino.start(channelsToRead);

            isRecording = true;

            Thread readThread = new Thread(new ReadThread());
            Thread analyzeThread = new Thread(new AnalyzeThread());
            saveThread = new Thread(new SaveThread());

            readThread.start();
            analyzeThread.start();
            saveThread.start();

            System.out.println("🎯 Hilos en ejecución (Read / Analyze / Save)");

        } catch (Throwable e) {
            //excepcion lanzar mensaje a la UI
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
        } catch (Exception e) { e.printStackTrace(); }

        try {
            System.out.println("⏳ Esperando a que termine SaveThread...");
            saveThread.join();
        } catch (InterruptedException e) { e.printStackTrace(); }


        zipFile = compressToZip(csvTempFile);
        System.out.println("📦 ZIP creado en: " + zipFile.getAbsolutePath());
    }

    public File getZipFile() {
        return zipFile;
    }

    public Signal buildSignalForPatient() {
        if (zipFile == null) {
            throw new IllegalStateException("zipFile is null. Did you call stopRecording() and compressToZip()?");
        }
        LocalDateTime now = LocalDateTime.now();
        int timeStamp = 0; // Placeholder for timestamp
        return new Signal(String.valueOf(now), fs, zipFile, String.valueOf(timeStamp));
    }

    public boolean isRecordingInterrupted() {
        return recordingInterrupted;
    }

    public boolean isRecording() {
        return isRecording;
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
                        if (f != null && f.analog != null) {
                            frameQueue.put(f);
                            saveQueue.put(f);
                        }
                    }
                }
            } catch (Exception e) {   System.out.println("❌ BITalino disconnected unexpectedly!");
                try {
                    handleBitalinoDisconnection();
                } catch (BITalinoException ex) {
                    throw new RuntimeException(ex);
                }
            }

            System.out.println("🔴 [ReadThread] Finalizado");
        }
    }

    private void handleBitalinoDisconnection() throws BITalinoException {
        isRecording = false;
        try {
            if (bitalino != null) {
                bitalino.stop();
                bitalino.close();
            }
        }catch (Exception ignored) {}

        try {
                saveThread.join(); // esperar a que guarde todo lo que ya tenía
                zipFile = compressToZip(csvTempFile);
                System.out.println("📦 Partial ZIP created: " + zipFile.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        recordingInterrupted= true;
    }


    private class AnalyzeThread implements Runnable {
        @Override
        public void run() {
            AlarmManager alarmManager = new AlarmManager();
            analyzer = new ECGRealTimeAnalyzer(fs, new ECGRealTimeAnalyzer.HRListener() {
                @Override
                public void onHeartRate(double hr) {
                    System.out.println("HR: " + hr);
                }
                @Override
                public void onPeakDetected(int sampleIndex) {
                    System.out.println("R-peak at sample: " + sampleIndex);
                }
                @Override
                public void onBradycardia(double hr) {
                    System.out.println("⚠ BRADICARDIA: " + hr);
                }
                @Override
                public void onTachycardia(double hr) {
                    System.out.println("⚠ TAQUICARDIA: " + hr);
                }

            },   alarmManager);
            boolean anomaly;
            try {
                while (isRecording || !frameQueue.isEmpty()) {
                    Frame f = frameQueue.take();
                    anomaly = analyzeSignals(f);
                    if (anomaly) {
                        controller.onAnomalyDetected();
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }

            System.out.println("🟣 [AnalyzeThread] Finalizado");
        }
    }

    //TODO: revisar
    //Alternative analyzeDignal function implementing the ecgProcessor and accProcessor
    private void analyzeSignals2(Frame f) {
        long ts = System.currentTimeMillis();

        double ecg = f.analog[0];
        double ax = f.analog[1];
        double ay = f.analog[2];
        double az = f.analog[3];

        ecgProcessor.addSample(ecg, ts);
        accProcessor.addSample(ax, ay, az, ts);

        double hr = ecgProcessor.getCurrentHeartRate();
        boolean hrRising = ecgProcessor.isHeartRateRising();
        MovementState movement = accProcessor.getMovementState();

        detectionManager.update(ts, hr, hrRising, movement);
    }

    private boolean analyzeSignals(Frame f) {
        boolean anomaly =false;
        double ecg = f.analog[1]; // ECG en A2
        double acc= f.analog[0]; // Acelerómetro en A1
        System.out.println(Arrays.toString(f.analog));

        anomaly=analyzer.addSample(ecg);
        double accMagnitude = acc;

        if(anomaly && accMagnitude > 1200){
            System.out.println("⚠ POSIBLE ATAQUE: " + accMagnitude);
            return true;
        }
        return anomaly;
    }

    private class SaveThread implements Runnable {
        @Override
        public void run() {
            System.out.println("💾 [SaveThread] Iniciado");

            try {
                csvTempFile = File.createTempFile("bitalino_", ".csv");
                csvTempFile.deleteOnExit();

                try (FileWriter writer = new FileWriter(csvTempFile)) {
                    while (isRecording || !saveQueue.isEmpty()) {
                        Frame f = saveQueue.take();

                        writer.write(
                                f.analog[1] + ";" + // ECG en A2
                                        f.analog[0] + "\n" // Acelerómetro en A1
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
    public static String zipToBase64(File zipFile) throws Exception {
        byte[] bytes = Files.readAllBytes(zipFile.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String buildUploadSignalJson(File zipFile, int patientId, int samplingRate) throws Exception {
 // todo los q hagan falta para la base de datos separar metadata de la señal y el user id
        UploadSignalRequest req = new UploadSignalRequest();

        // --- Metadata ---
        req.metadata = new UploadSignalRequest.Metadata();
        req.metadata.patient_id = patientId;
        req.metadata.sampling_rate = samplingRate;
        req.metadata.timestamp = LocalDateTime.now().toString();

        // --- ZIP contenido ---
        req.filename = zipFile.getName();
        req.data = zipToBase64(zipFile);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(req);
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
    public class UploadSignalRequest {
        public String type = "UPLOAD_SIGNAL";
        public Metadata metadata;
        public String compression = "zip-base64";
        public String filename;
        public String data; // ZIP en base64

        public static class Metadata {
            public int patient_id;
            public int sampling_rate;
            public int duration_seconds;
            public String[] channels;
            public String timestamp;
        }
    }

}

