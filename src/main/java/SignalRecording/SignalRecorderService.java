package SignalRecording;

import BITalino.BITalino;
import Events.BITalinoDisconnectedEvent;
import Events.UIEventBus;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;
/**
 * Service responsible for managing the end-to-end acquisition pipeline
 * of physiological signals from a BITalino device.
 * <p>
 * This class encapsulates:
 * <ul>
 *     <li>Bluetooth connection with BITalino</li>
 *     <li>Starting/stopping the acquisition</li>
 *     <li>Launching the multi-threaded processing pipeline:
 *          <ul>
 *              <li>ReadThread – reads frames from BITalino in real time</li>
 *              <li>AnalyzeThread – processes ECG/ACC and updates detection logic</li>
 *              <li>SaveThread – writes samples into a temporary CSV</li>
 *          </ul>
 *     </li>
 *     <li>Interrupt handling in case of Bluetooth disconnection</li>
 *     <li>ZIP generation of recorded samples for upload</li>
 * </ul>
 * Each recording creates a temporary CSV file which is compressed into a ZIP
 * when recording stops or when the device disconnects unexpectedly.
 */
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
    /**
     * Creates a new SignalRecorderService tied to a specific BITalino MAC address.
     *
     * @param MAC_ADDRESS Bluetooth MAC address of the target BITalino device
     */
    public SignalRecorderService(String MAC_ADDRESS) {
        SignalRecorderService.MAC_ADDRESS = MAC_ADDRESS;
    }
    /**
     * Returns the sampling frequency used for acquisition.
     *
     * @return sampling frequency in Hz (default: 100 Hz)
     */
    public int getFs() {
        return fs;
    }
    /**
     * Establishes a Bluetooth connection with the BITalino device using the
     * configured MAC address and sampling frequency.
     * <p>
     * On success, the {@code connected} flag is set to {@code true}.
     * On failure, an error message is logged and the service remains disconnected.
     *  @throws RecordingException if the MAC is invalid or the device cannot be reached.
     */
    public void bitalinoConnect() throws RecordingException {
        try {
            if (MAC_ADDRESS == null || MAC_ADDRESS.isBlank()) {
                throw new RecordingException(RecordingErrors.INVALID_MAC_ADDRESS);
            }
            System.out.println("🔌 Connecting to BITalino...");
            bitalino = new BITalino();
            bitalino.open(MAC_ADDRESS, fs);
            connected = true;
            System.out.println("✅ Connection established.");
        }
        catch (IllegalArgumentException invalidMac) {
            connected = false;
            throw new RecordingException(RecordingErrors.INVALID_MAC_ADDRESS);

        } catch (BITalinoException btError) {
            connected = false;

            if (btError.getMessage().contains("SAMPLING") ||
                    btError.getMessage().contains("frequency")) {
                throw new RecordingException(RecordingErrors.INVALID_SAMPLING_FREQUENCY);
            }

            throw new RecordingException(RecordingErrors.CONNECTION_FAILED);

        } catch (Exception e) {
            connected = false;
            throw new RecordingException(RecordingErrors.UNEXPECTED_EXCEPTION);
        }
    }
    /**
     * Starts the real-time acquisition from BITalino and launches the processing
     * pipeline threads (read, analyze, save).
     * <p>
     * This method requires the device to be successfully connected; otherwise, a
     * @throws RecordingException if the device is not connected, the channel
     *  configuration is invalid, or communication with BITalino fails.
     */
    public void startRecording() throws RecordingException{
        if (!connected || bitalino == null) {
            throw new RecordingException(RecordingErrors.DEVICE_NOT_CONNECTED);
        }
        try {
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

            System.out.println("🎯 Threads in execution (Read / Analyze / Save)");

        } catch (IllegalArgumentException invalidChannels) {
            // Channels configuration is invalid
            throw new RecordingException(RecordingErrors.INVALID_CHANNEL_CONFIGURATION);

        } catch (BITalinoException btError) {
            // BITalino-specific communication problem
            throw new RecordingException(RecordingErrors.LOST_COMMUNICATION);

        } catch (Throwable unexpected) {
            // Any other unexpected failure (NullPointer, IO, etc.)
            throw new RecordingException(RecordingErrors.UNEXPECTED_EXCEPTION);

        }
    }
    /**
     * Stops the acquisition pipeline and gracefully terminates all processing threads.
     * <p>
     * Steps:
     * <ul>
     *     <li>Stops the recording state</li>
     *     <li>Sends poison pills to the analysis and saving queues</li>
     *     <li>Stops and closes BITalino safely</li>
     *     <li>Joins Read / Analyze / Save threads</li>
     *     <li>Creates a ZIP from the temporary CSV</li>
     *     <li>Posts {@link BITalinoDisconnectedEvent} for UI updates</li>
     * </ul>
     *
     * @throws RecordingException if stopping fails due to thread crashes,
     *                            file errors, or unexpected device behavior
     */
    public void stopRecording() throws RecordingException {
        if (!isRecording && !recordingInterrupted) {
            System.out.println("⚠ stopRecording() called but it was already not recording");
            throw new RecordingException(RecordingErrors.STOPPED_WHEN_NOT_RECORDING);
        }

        if (recordingInterrupted) {
            System.out.println("⚠ stopRecording() called after disconnection.");
            return;
        }

        isRecording = false;
        System.out.println("🛑 Stopping acquisition...");

        try {frameQueue.put(POISON_PILL);} catch (InterruptedException ignored) {  throw new RecordingException(RecordingErrors.QUEUE_PUT_ERROR);}
        try {saveQueue.put(POISON_PILL);} catch (InterruptedException ignored) {  throw new RecordingException(RecordingErrors.QUEUE_PUT_ERROR);}

        // Cerrar BITalino si sigue vivo
        try {
            if (connected && bitalino != null) {
                bitalino.stop();
                bitalino.close();
            }
        } catch (Exception e) {  throw new RecordingException(RecordingErrors.DEVICE_READ_ERROR); }
        finally { connected = false; }

        try {
            if (readThread != null) {
                System.out.println("⏳ Waiting for ReadThread...");
                readThread.join();
            }
            if (analyzeThread != null) {
                System.out.println( "⏳ Waiting for AnalyzeThread...");
                analyzeThread.join();
            }
            if (saveThread != null) {
                System.out.println("⏳ Waiting for SaveThread...");
                saveThread.join();
            }
        } catch (InterruptedException e) {
            throw new RecordingException(RecordingErrors.UNEXPECTED_EXCEPTION);
        }
        if (csvTempFile != null && csvTempFile.exists()) {
            zipFile = compressToZip(csvTempFile);
            if (zipFile != null) System.out.println("📦 ZIP created in: " + zipFile.getAbsolutePath());
        } else {
            throw new RecordingException(RecordingErrors.ZIP_CREATION_FAILED);
        }
        UIEventBus.BUS.post(new BITalinoDisconnectedEvent());
    }
    /**
     * Returns the ZIP file generated after stopping the recording.
     * This ZIP contains the CSV data recorded during the acquisition.
     *
     * @return ZIP file, or {@code null} if no file is available
     */
    public File getZipFile() {
        return zipFile;
    }
    /**
     * Indicates whether the BITalino device is currently connected.
     *
     * @return {@code true} if connected; {@code false} otherwise
     */
    public boolean isConnected() {
        return connected;
    }


    // ------------------------ THREADS ------------------------

    private class ReadThread implements Runnable {

        @Override
        public void run() {

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
                    // Unexpected disconnection happened during active acquisition
                    recordingInterrupted = true;
                    isRecording = false;
                    connected = false;

                    stopAfterDisconnection();

                    // Ensure other pipeline threads are terminated
                    try { frameQueue.put(POISON_PILL); } catch (InterruptedException ignored) {}
                    try { saveQueue.put(POISON_PILL); } catch (InterruptedException ignored) {}

                }
            }
        }
    }


    private void stopAfterDisconnection() {

        isRecording = false;
        recordingInterrupted = true;
        connected = false;

        // Send poison pills to terminate AnalyzeThread and SaveThread
        try { frameQueue.put(POISON_PILL); } catch (InterruptedException ignored) {}
        try { saveQueue.put(POISON_PILL); } catch (InterruptedException ignored) {}

        try {
            if (bitalino != null) {
                bitalino.stop();
                bitalino.close();
            }
        } catch (Exception e) {
            // Device was already disconnected or dead – safe to ignore;
        }

        // Wait ONLY for Analyze and Save threads (ReadThread is calling this method)
        try {
            if (analyzeThread != null) analyzeThread.join();
            if (saveThread != null)    saveThread.join();
        } catch (InterruptedException ignored) {}

        // Attempt to generate a partial ZIP with whatever data is available
        if (csvTempFile != null && csvTempFile.exists()) {
            zipFile = compressToZip(csvTempFile);
            if (zipFile != null) UIEventBus.BUS.post(new BITalinoDisconnectedEvent());
        }
    }

    private class AnalyzeThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Frame f = frameQueue.take();
                    if (f == POISON_PILL) break;
                    analyzeSignals(f);
                }
            } catch (InterruptedException ignored) {
                // Thread was interrupted; safe to exit
            } catch (Exception ignored) {
                // Any unexpected exception during analysis should not crash the pipeline
                // No logging or printing here (handled externally if needed)
            }
        }
    }

    //TODO: revisar
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
            try {
                csvTempFile = File.createTempFile("bitalino_", ".csv");
                csvTempFile.deleteOnExit();
               try (FileWriter writer = new FileWriter(csvTempFile)) {
                    while (true) {
                        Frame f = saveQueue.take();
                        if (f == POISON_PILL) break;
                        writer.write(
                                f.analog[0] + ";" + (f.analog.length > 1 ? f.analog[1] : 0) + "\n"
                        );
                    }
                }
            } catch (Exception ignored) { }

            System.out.println("🟦 [SaveThread] Ended");
        }
    }
    /**
     * Compresses the provided CSV file into a temporary ZIP archive.
     * This method is fault-tolerant and never throws an exception,
     * as it is used by worker threads and by the post-processing pipeline.
     *
     * @param csvFile the CSV file containing the recorded samples
     * @return a ZIP file containing the CSV, or {@code null} if compression fails
     */
    public File compressToZip(File csvFile) {
        if (csvFile == null || !csvFile.exists()) {
            return null;
        }

        try {
            File zip = File.createTempFile("bitalino_", ".zip");
            zip.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(zip);
                 java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);
                 FileInputStream fis = new FileInputStream(csvFile)) {

                zos.putNextEntry(new java.util.zip.ZipEntry(csvFile.getName()));

                byte[] buffer = new byte[1024];
                int len;

                while ((len = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
            }

            return zip;

        } catch (Exception ignored) {
            // Any failure results in returning null; calling code handles this gracefully
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

