package ceu.biolab.sendCharacters.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerReceiveCharactersViaNetwork {

    public static void main(String args[]) throws IOException {
        int byteRead;
        
        //Create a service that is waiting in port 9000
        ServerSocket serverSocket = new ServerSocket(9000);
        
        //Thie executes when we have a client
        Socket socket = serverSocket.accept();
        //Read from the client
        InputStream inputStream = socket.getInputStream();
        while (true) {
            byteRead = inputStream.read();        
            //We reaad until is finished the connection or character 'x'
            if (byteRead == -1 || byteRead == 'x') {
                System.out.println("Character reception finished");
                releaseResources(inputStream, socket, serverSocket);
                System.exit(0);
            }
            char caracter = (char) byteRead;
            System.out.print(caracter);
            System.out.print(" ");
        }
    }

    private static void releaseResources(InputStream inputStream, Socket socket,
            ServerSocket socketServidor) {
      
        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerReceiveCharactersViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerReceiveCharactersViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socketServidor.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerReceiveCharactersViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}