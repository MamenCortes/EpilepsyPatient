package ceu.biolab.sendUDP;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientUDP {

    public static void main(String args[]) {
        String string = "Sending datagram in the date: " + new Date();
        DatagramSocket socket = null;
        try {
            byte[] buffer = string.getBytes();
            InetAddress destination = InetAddress.getByName("localhost");
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length,
                    destination, 9000);
            socket = new DatagramSocket();
            socket.send(datagram);
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
        }  finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
