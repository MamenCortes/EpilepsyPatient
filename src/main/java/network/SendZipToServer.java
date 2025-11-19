package network;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
public class SendZipToServer {
    private final String ip;
    private final int port;

    public SendZipToServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean sendZipToServer(File zipFile) {
        try (Socket socket = new Socket(ip, port);
             OutputStream out = socket.getOutputStream();
             FileInputStream fis = new FileInputStream(zipFile))
             {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getIP() {
        return ip;
    }

    public int getPORT() {
        return port;
    }



}