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
        //TODO: noted from class added, check
        //IT's important to execute the server before the client and not the other way around
        //Client and server have to specify the same port
        //Create a service that is waiting in port 9000
        ServerSocket serverSocket = new ServerSocket(9000);

        //To send serialized objects, we need to use the decorators seen in Java 1 using decorators
        //Thie executes when we have a client
        Socket socket = serverSocket.accept();
        //Read from the client
        InputStream inputStream = socket.getInputStream();
        while (true) {
            //Is waiting for inputs from the client
            byteRead = inputStream.read();        
            //We reaad until is finished the connection or character 'x'
            if (byteRead == -1 || byteRead == 'x') {
                System.out.println("Character reception finished");
                releaseResources(inputStream, socket, serverSocket);
                System.exit(0);
            }
            char caracter = (char) byteRead;
            System.out.print(caracter);
            System.out.print(" "); //printed with a space
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