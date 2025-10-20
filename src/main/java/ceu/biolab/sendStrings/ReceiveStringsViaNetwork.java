package ceu.biolab.sendStrings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveStringsViaNetwork {

    public static void main(String args[]) throws IOException {

        //Commented: Single thread connection
        ServerSocket serverSocket = new ServerSocket(9009);

        while (true) { //add to make it accept sequential connections
            Socket socket = serverSocket.accept();
            System.out.println("Connection client created");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            System.out.println("Text Received:\n");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase().contains("stop")) { //When we receive the stop, the conexion finishes
                    break;
                    //System.out.println("Stopping the server");
                    //releaseResources(bufferedReader, socket, serverSocket);
                    //System.exit(0);
                }
                System.out.println(line);
            }
        }
    }

    private static void releaseResources(BufferedReader bufferedReader,
            Socket socket, ServerSocket socketServidor) {
        try {
            bufferedReader.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveStringsViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveStringsViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socketServidor.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveStringsViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
