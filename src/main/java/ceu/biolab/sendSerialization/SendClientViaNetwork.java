package ceu.biolab.sendSerialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendClientViaNetwork {

    public static void main(String args[]) {
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        Socket socket = null;

        Client[] clients = new Client[3];
        clients[0] = new Client("Alberto Montoya", 67, 40);
        clients[1] = new Client("Javier Otero", 69, 9000);
        clients[2] = new Client("Luisa Santos", 71, 3000);

        try {
            socket = new Socket("localhost", 9000);
            outputStream = socket.getOutputStream();
        } catch (IOException ex) {
            System.out.println("It was not possible to connect to the server.");
            System.exit(-1);
            Logger.getLogger(SendClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(clients[0]);
            objectOutputStream.writeObject(clients[1]);
            objectOutputStream.writeObject(clients[2]);
            objectOutputStream.flush();
        } catch (IOException ex) {
            System.out.println("Unable to write the objects on the server.");
            Logger.getLogger(SendClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            releaseResources(objectOutputStream, socket);

        }
    }

    private static void releaseResources(ObjectOutputStream objectOutputStream, Socket socket) {
        try {
            objectOutputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(SendClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(SendClientViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
