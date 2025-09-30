package ceu.biolab.sendBinary;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendBinaryDataViaNetwork {

    public static void main(String args[]) throws IOException {
        Socket socket = new Socket("localhost", 9000);
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        dataOutputStream.writeInt(40);
        dataOutputStream.writeFloat(4.0F);
        dataOutputStream.writeUTF("Hello");
        dataOutputStream.writeBoolean(true);
        releaseResources(dataOutputStream, outputStream, socket);
    }

    private static void releaseResources(DataOutputStream dataOutputStream,
            OutputStream outputStream, Socket socket) {
        try {
            dataOutputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(SendBinaryDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(SendBinaryDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(SendBinaryDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
