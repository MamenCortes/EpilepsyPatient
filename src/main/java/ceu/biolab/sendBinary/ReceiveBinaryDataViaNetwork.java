package ceu.biolab.sendBinary;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveBinaryDataViaNetwork {

    public static void main(String args[]) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9000);
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        System.out.println(dataInputStream.readInt());
        System.out.println(dataInputStream.readFloat());
        System.out.println(dataInputStream.readUTF());
        System.out.println(dataInputStream.readBoolean());

        releaseResources(dataInputStream, inputStream, socket, serverSocket);

    }

    private static void releaseResources(DataInputStream dataInputStream, InputStream inputStream,
            Socket socket, ServerSocket socketServidor) {

        try {
            dataInputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveBinaryDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveBinaryDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveBinaryDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socketServidor.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveBinaryDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
