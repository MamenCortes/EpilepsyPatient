package network;

import org.junit.Test;
import ui.SignalRecorderService;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertTrue;
public class SendZipToServerTest {
    private File createRealZip() throws Exception {
        File zip = File.createTempFile("test_", ".zip");

        try (FileOutputStream fos = new FileOutputStream(zip);
             java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos)) {

            ZipEntry entry = new ZipEntry("content.txt");
            zos.putNextEntry(entry);
            zos.write("HELLO_WORLD".getBytes());
            zos.closeEntry();
        }

        return zip;
    }

    @Test
    public void testSendZipAndServerReceives() throws Exception {

        // 1. Crear ZIP válido
        File testZip = createRealZip();
        System.out.println("ZIP creado: " + testZip.getAbsolutePath());
        System.out.println("Bytes enviados: " + testZip.length());

        int freePort = 9000;

        SendZipToServer client = new SendZipToServer("localhost", freePort);
        boolean ok = client.sendZipToServer(testZip);

        assertTrue("El cliente debe enviar correctamente.", ok);




    }


}
