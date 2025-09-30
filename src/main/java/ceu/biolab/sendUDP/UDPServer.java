package ceu.biolab.sendUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPServer {

    private static byte[] buffer;
    private static byte[] data;

    public static void main(String args[]) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(9000);
            buffer = new byte[1024];
            while (true) {
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagram);
                InetAddress originHost = datagram.getAddress();
                int destinationPort = datagram.getPort();
                data = datagram.getData();
                String cadena = new String(data, 0, datagram.getLength());
                System.out.println("Welcome to the server. Sent from"
                        + originHost + " through the port "
                        + destinationPort + " the message: " + cadena);
            }
        } catch (SocketException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }  finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}