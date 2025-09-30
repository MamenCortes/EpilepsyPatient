package ceu.biolab.threadServer;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    public static void main(String args[]) throws IOException {
        //You should change localhost by the IP address 
        //We are connecting to a "service" in an IP and port 9000
        Socket socket = new Socket("localhost", 9000);
        OutputStream outputStream = socket.getOutputStream();

        String currentDirectory = System.getProperty("user.dir");

        // Print the current working directory
        System.out.println("Current working directory: " + currentDirectory);

        //File To Read
        File file = new File("src/main/resources/FileToRead.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        int character;
        while ((character = br.read()) != -1) {
            System.out.println(character);
            outputStream.write(character);
            outputStream.flush();
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        outputStream.flush();
        releaseResources(outputStream, br, socket);
        System.exit(0);
    }

    private static void releaseResources(OutputStream outputStream,
            BufferedReader br, Socket socket) {
        try {
            try {
                br.close();

            } catch (IOException ex) {
                Logger.getLogger(Client.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            try {
                outputStream.close();

            } catch (IOException ex) {
                Logger.getLogger(Client.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            socket.close();

        } catch (IOException ex) {
            Logger.getLogger(Client.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
