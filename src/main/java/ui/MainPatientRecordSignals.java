package ui;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainPatientRecordSignals {
    public static void main(String[] args) throws IOException {
        System.out.println("=== BITalino Multithread Test ===");
        System.out.println("1️⃣  Hilo lector   → Lee frames del BITalino");
        System.out.println("2️⃣  Hilo analizador → Procesa señales y crea JSON");
        System.out.println("3️⃣  Hilo emisor   → Envía al servidor (simulado)");
        System.out.println("--------------------------------------------");

        SignalRecorderService recorder = new SignalRecorderService();

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

        System.out.println("✅ Programa finalizado correctamente.");
    }
}
