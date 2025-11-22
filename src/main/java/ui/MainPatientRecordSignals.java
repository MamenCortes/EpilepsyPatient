package ui;



import network.SendZipToServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainPatientRecordSignals {
    public static void main(String[] args) throws IOException {
        System.out.println("=== BITalino Multithread Test ===");
        System.out.println("1️⃣  Hilo lector   → Lee frames del BITalino");
        System.out.println("2️⃣  Hilo analizador → Procesa señales y crea JSON");
        System.out.println("3️⃣  Hilo emisor   → Envía al servidor (simulado)");
        System.out.println("--------------------------------------------");
        String MacAddress = "20:16:11:26:69:56"; // Reemplaza con la dirección MAC de tu dispositivo BITalino
        SignalRecorderService recorder = new SignalRecorderService(MacAddress );

        // Inicia la grabación
        recorder.startRecording();

        // Espera órdenes del usuario
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("🟢 Grabando... escribe 'stop' para detener.");

        while (true) {
            String line = br.readLine();
            if (line == null) continue;

            if (line.equalsIgnoreCase("stop")) {
                System.out.println("🛑 Comando recibido: deteniendo hilos...");
                recorder.stopRecording();
                break;
            }
        }
        System.out.println("Introduce la IP del servidor:");
        String IP = br.readLine();
        System.out.println("Introduce el puerto del servidor:");
        int PORT = Integer.parseInt(br.readLine());
        File zipFile = recorder.getZipFile();
        SendZipToServer send =new SendZipToServer(IP,PORT);
        send.sendZipToServer(zipFile);
        System.out.println("✅ Programa finalizado correctamente.");
    }
}
