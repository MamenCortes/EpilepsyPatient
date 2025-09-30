package ceu.biolab.threadServerAnotherrExample;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerNoThreads {

    public static void main(String args[]) throws IOException {
        int byteRead;
        boolean stopServer = false;
        //Create a service that is waiting in port 9000
        ServerSocket serverSocket = new ServerSocket(9000);

        //Thie executes when we have a client
        while (!stopServer) {
            Socket socket = serverSocket.accept();
            //Read from the client
            InputStream inputStream = socket.getInputStream();
            boolean stopClient = false;
            while (!stopClient) {
                byteRead = inputStream.read();
                //To stop the server
                if (byteRead == '!') {
                    System.out.println("StoppingServer");
                    stopServer = true;
                }
                //We reaad until is finished the connection or character 'x'
                if (byteRead == -1 || byteRead == 'x' || byteRead == '!') {
                    System.out.println("Client character reception finished");
                    stopClient = true;
                    releaseResourcesClient(inputStream, socket);
                }
                char caracter = (char) byteRead;
                System.out.print(caracter);
                System.out.print(" ");
            }
        }
        releaseResourcesServer(serverSocket);
    }

    private static void releaseResourcesClient(InputStream inputStream, Socket socket) {

        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerNoThreads.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerNoThreads.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void releaseResourcesServer(ServerSocket serverSocket) {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerNoThreads.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
