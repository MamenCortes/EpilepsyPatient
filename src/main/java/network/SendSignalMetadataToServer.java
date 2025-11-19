package network;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendSignalMetadataToServer {

    private final String ip;
    private final int port;

    public SendSignalMetadataToServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean sendMetadataJson(String json) {
        try (Socket socket = new Socket(ip, port);
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream()), true)) {

            writer.println(json);
            writer.flush();

            System.out.println("✅ Metadata JSON sent to server.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

