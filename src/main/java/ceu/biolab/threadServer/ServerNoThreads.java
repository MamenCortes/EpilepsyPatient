package ceu.biolab.threadServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerNoThreads {

    public static void main(String args[]) throws IOException {
        int byteRead;
        ServerSocket serverSocket = new ServerSocket(9000);

        //Thie executes when we have a client
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                //Read from the client
                InputStream inputStream = socket.getInputStream();

                while ((byteRead = inputStream.read()) != -1) {
                    char caracter = (char) byteRead;
                    System.out.print(caracter);
                }
            }
        } finally {
            releaseResourcesServer(serverSocket);
        }
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
