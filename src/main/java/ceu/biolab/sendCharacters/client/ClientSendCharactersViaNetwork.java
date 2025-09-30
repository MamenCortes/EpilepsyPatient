package ceu.biolab.sendCharacters.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSendCharactersViaNetwork {

    public static void main(String args[]) throws IOException {
        int byteRead;
        //Redirect the console
        InputStream console = (System.in);
        //You should change localhost by the IP address 
        //We are connecting to a "service" in an IP and port 9000
        Socket socket = new Socket("localhost", 9000);
        OutputStream outputStream = socket.getOutputStream();

        while (true) {
            //Read from our console
            byteRead = console.read();
            //And send it to the server
            outputStream.write(byteRead);
            if (byteRead == -1 || byteRead == 'x') {
                System.out.println("Finish");
                releaseResources(outputStream, console, socket);
                System.exit(0);
            }
        }
    }

    private static void releaseResources(OutputStream outputStream,
            InputStream console, Socket socket) {
        try {
            try {
                console.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientSendCharactersViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                outputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientSendCharactersViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientSendCharactersViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
