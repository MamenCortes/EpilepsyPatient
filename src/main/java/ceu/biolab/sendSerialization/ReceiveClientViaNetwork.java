package ceu.biolab.sendSerialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveClientViaNetwork {

    public static void main(String args[]) {
        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(9000);
            socket = serverSocket.accept();
            inputStream = socket.getInputStream();
            System.out.println("Connection from the direction "
                    + socket.getInetAddress());
        } catch (IOException ex) {
            System.out.println("It was not possible to start the server. Fatal error.");
            Logger.getLogger(ReceiveClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        try {
            objectInputStream = new ObjectInputStream(inputStream);
            Object tmp;
            while ((tmp = objectInputStream.readObject()) != null) {
                Client persona = (Client) tmp;
                System.out.println(persona.toString());
            }
        } catch (EOFException ex) {
            System.out.println("All data have been correctly read.");
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Unable to read from the client.");
            Logger.getLogger(ReceiveClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            releaseResources(objectInputStream, socket, serverSocket);
        }
    }

    private static void releaseResources(ObjectInputStream objectInputStream, Socket socket, ServerSocket serverSocket) {
        try {
            objectInputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
