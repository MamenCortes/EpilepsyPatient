package ceu.biolab.threadsWebServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

public class WebServer {

    private static boolean stop = false;

    public static void main(String args[]) throws IOException {
        Socket socket = null;
        try {
            ServerSocket serverSocket = new ServerSocket(9000);
            serverSocket.setSoTimeout(3000);
            while (!stop) {
                try {
                    socket = serverSocket.accept();
                    Connection connection = new Connection(socket);
                    connection.start();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class Connection extends Thread {

        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;

        public Connection(Socket socketCliente) {
            this.socket = socketCliente;
        }

        @Override
        public void run() {
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Welcome to the server\n\n");
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                while (reader.ready()) {
                    String textoRecibido = reader.readLine();
                    System.out.println(textoRecibido);
                    if (textoRecibido.toLowerCase().contains("hello")) {
                        writer.println("\nHello");
                    }
                    else if (textoRecibido.toLowerCase().contains("stop")) {
                        System.out.println("Stopping the server");
                        writer.println("\nReceived command to stop server");
                        stop = true;
                    } else if (textoRecibido.toLowerCase().contains("get")
                            && textoRecibido.toLowerCase().contains("list")) {
                        listarDirectorio(textoRecibido);
                    }
                }
                System.out.println("cerrando cerrando cerrando");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void listarDirectorio(String textoRecibido) {
            String comando = textoRecibido.replace("%20", " ").toLowerCase();
            int principioComando = comando.indexOf("list")
                    + "list".length() + 1;
            int finComando = comando.indexOf("http");
            String directorioString = comando.substring(principioComando, finComando);

            File f = new File(directorioString);
            if (!f.exists()) {
                writer.println("Wrong directory!");
            }
            for (File ff : f.listFiles()) {
                writer.println(ff.getName() + " " + new Date(ff.lastModified()));
            }
        }
    }
}
