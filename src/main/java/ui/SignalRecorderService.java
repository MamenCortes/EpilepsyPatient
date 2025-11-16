package ui;

import ceu.biolab.BITalino.BITalino;
import ceu.biolab.BITalino.Frame;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class SignalRecorderService {

    private static final String MAC_ADDRESS = "98:D3:C1:FD:2F:EA";

    private BITalino bitalino;
    private volatile boolean isRecording = false;

    private File csvTempFile;
    private File zipFile;

    private Thread saveThread;

    private final BlockingQueue<Frame> frameQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Frame> saveQueue = new LinkedBlockingQueue<>();

    public void startRecording() {
        try {
            System.out.println("🔌 Conectando al BITalino...");
            bitalino = new BITalino();
            bitalino.open(MAC_ADDRESS, 1000);
            System.out.println("✅ Conexión establecida.");

            int[] channelsToRead = {1, 2, 3, 4};
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

        // Ahora sí, ya se escribió todo el CSV
        zipFile = compressToZip(csvTempFile);
        System.out.println("📦 ZIP creado en: " + zipFile.getAbsolutePath());
    }

    public File getZipFile() {
        return zipFile;
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
                            System.out.println("📡 ECG recibido: " + f.analog[0]);
                            frameQueue.put(f);
                            saveQueue.put(f);
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }

            System.out.println("🔴 [ReadThread] Finalizado");
        }
    }

    private class AnalyzeThread implements Runnable {
        @Override
        public void run() {
            System.out.println("🟣 [AnalyzeThread] Iniciado");

            try {
                while (isRecording || !frameQueue.isEmpty()) {
                    Frame f = frameQueue.take();
                    analyzeSignals(f);
                }
            } catch (Exception e) { e.printStackTrace(); }

            System.out.println("🟣 [AnalyzeThread] Finalizado");
        }
    }

    private void analyzeSignals(Frame f) {
        double ecg = f.analog[0];
        double ax = f.analog[1];
        double ay = f.analog[2];
        double az = f.analog[3];

        double accMagnitude = Math.sqrt(ax * ax + ay * ay + az * az);

        if (ecg > 800) System.out.println("⚠️ ECG alto: " + ecg);
        if (accMagnitude > 1200) System.out.println("⚡ Movimiento brusco: " + accMagnitude);
        if (ecg > 900 && accMagnitude > 1200) System.out.println("🚨 Posible ataque detectado");
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
                                f.analog[0] + ";" +
                                        f.analog[1] + ";" +
                                        f.analog[2] + ";" +
                                        f.analog[3] + "\n"
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
        SignalRecorderService service = new SignalRecorderService();

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
        SignalRecorderService service = new SignalRecorderService();

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

