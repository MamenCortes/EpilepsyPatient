package BITalino;

import signalRecording.RecordingException;
import signalRecording.SignalRecorderService;

import java.io.File;

public class BitalinoConnectionTest {
    public static void main(String[] args) {

        String mac = "98:D3:C1:FD:2F:EA";

        System.out.println("=== BITalino Connection Test ===");

        SignalRecorderService service = new SignalRecorderService(mac);

        // 1. Connect to BITalino
        System.out.println("→ Connecting to BITalino...");

        try {
            service.bitalinoConnect();
        } catch (RecordingException e) {
            System.out.println("[ERROR] Unable to connect: " + e.getError().getTitle());
            System.out.println("Details: " + e.getError().getDescription());
            System.out.println("=== Test aborted ===");
            return;
        }

        System.out.println("✔ Connection established.");

        // 2. Start acquisition
        System.out.println("→ Starting acquisition...");

        try {
            service.startRecording();
        } catch (RecordingException e) {
            System.out.println("[ERROR] Could not start recording: " + e.getError().getTitle());
            System.out.println("Details: " + e.getError().getDescription());
            System.out.println("=== Test aborted ===");
            return;
        }

        System.out.println("✔ Recording for 3 seconds...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}


        // 3. Stop acquisition
        System.out.println("→ Stopping acquisition...");

        try {
            service.stopRecording();
        } catch (RecordingException e) {
            System.out.println("[ERROR] Could not stop recording: " + e.getError().getTitle());
            System.out.println("Details: " + e.getError().getDescription());
            System.out.println("=== Test aborted ===");
            return;
        }

        // 4. Retrieve ZIP
        File zip = service.getZipFile();

        if (zip != null && zip.exists()) {
            System.out.println("✔ ZIP generated successfully:");
            System.out.println("  " + zip.getAbsolutePath());
        } else {
            System.out.println("⚠ No ZIP file was generated.");
        }

        System.out.println("=== End of Test ===");
    }

}