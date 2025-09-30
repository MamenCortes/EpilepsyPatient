package ceu.biolab.threadServerAnotherrExample;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThreads {

    public static void main(String args[]) throws IOException {
        //Create a service that is waiting in port 9000
        ServerSocket serverSocket = new ServerSocket(9000);
        try {
            while (true) {
                //Thie executes when we have a client
                Socket socket = serverSocket.accept();
                new Thread(new ServerClientThread(socket)).start();
            }
        } finally {
            releaseResourcesServer(serverSocket);
        }
    }

    private static void releaseResourcesClient(InputStream inputStream, Socket socket) {

        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThreads.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThreads.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void releaseResourcesServer(ServerSocket serverSocket) {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThreads.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class ServerClientThread implements Runnable {
        int byteRead;
        Socket socket;
        private ServerClientThread(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                //Read from the client
                inputStream = socket.getInputStream();
                boolean stopClient = false;
                while (!stopClient) {
                    byteRead = inputStream.read();
                    //We reaad until is finished the connection or character 'x'
                    if (byteRead == -1 || byteRead == 'x' || byteRead == '!') {
                        System.out.println("Client character reception finished");
                        stopClient = true;
                    }
                    char caracter = (char) byteRead;
                    System.out.print(caracter);
                    System.out.print(" ");
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerThreads.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                releaseResourcesClient(inputStream, socket);
            }
        }
    }
}
