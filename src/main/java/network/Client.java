package network;

import com.google.gson.*;
import pojos.Doctor;
import pojos.Patient;
import pojos.User;
import ui.windows.Application;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client{
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    private Application appMain;
    private Gson gson = new Gson();
    private volatile Boolean running;
    //Estructura diseñada para comunicar threads entre sí de manera segura y sincronizada
    //permite que un thread meta mensajes en la cola (con put())
    //Y que otro thread los reciba (con take() o poll())
    //si no hay mensajes, take() se bloquea automáticamente, sin consumir CPU
    private BlockingQueue<JsonObject> responseQueue = new LinkedBlockingQueue<>();

    public Client(Application appMain) {
        this.appMain = appMain;
    }

    public Boolean connect(String ip, int port) {

        try {
            //socket = new Socket("localhost", 9009);
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            running = true;
            sendInitialMessage();
            startListener();
            return true;
        }catch(IOException e){
            //if(!socket.isConnected()){appMain.onServerDisconnected();}
            return false;
        }

    }

    /// Start a thread that listens for messages from the server
    /// If the server sends STOP_CLIENT, the connection is closed and also the app???
    public void startListener() {
        System.out.println("Listening for messages...");
        Thread listener = new Thread(() -> {
            try {
                String line;
                while (((line = in.readLine()) != null)&&running) {
                    //System.out.println("New message: " + line);
                    JsonObject json = gson.fromJson(line, JsonObject.class);

                    String type = json.get("type").getAsString();

                    if (type.equals("STOP_CLIENT")) {
                        System.out.println("Server requested shutdown");

                        //Send stop received (ACK)
                        JsonObject response = new JsonObject();
                        response.addProperty("type", "STOP_CLIENT_RECEIVED");
                        out.println(gson.toJson(response));
                        //stop client
                        stopClient(false);
                        break;
                    }

                    try {
                        responseQueue.put(json);
                    }catch (InterruptedException e){
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("type", "LOGIN_REQUEST_RESPONSE");
                        jsonObject.addProperty("status", "ERROR");
                        jsonObject.addProperty("message", "Error while processing login request");
                        responseQueue.add(jsonObject);
                    }

                }
            } catch (IOException ex) {
                System.out.println("Server connection closed: " + ex.getMessage());
                //if(running) appMain.onServerDisconnected();
            }
        });

        //listener.setDaemon(true); // client ends even if thread is running
        listener.start();
    }

    public Boolean isConnected(){
        if(socket == null){
            return false;
        }else {
            return socket.isConnected();
        }
    }

    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        out.println("Hi! I'm a new client!\n");
    }

    public void stopClient(boolean initiatedByClient) {
        if (initiatedByClient && socket != null && !socket.isClosed()) {
            // Only send STOP_CLIENT if CLIENT requested shutdown
            Map<String, Object> message = new HashMap<>();
            message.put("type", "STOP_CLIENT");
            String jsonMessage = gson.toJson(message);
            out.println(jsonMessage);
            System.out.println("Sent (client-initiated): " + jsonMessage);
        }

        System.out.println("Stopping client...");
        running = false;

        // Notify UI ONLY if the server disconnected
        if (!initiatedByClient) {
            appMain.onServerDisconnected();
        }

        releaseResources(out, in, socket);
    }


    //TODO: change to send Exception instead of MAP
    public Map<String, Object> login(String email, String password) throws IOException, InterruptedException {
        //String message = "LOGIN;" + email + ";" + password;
        Map<String, Object> login = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("access_permits", "Patient");

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOGIN_REQUEST");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message

        // Read the response
        //String responseLine = in.readLine();
        JsonObject response;

        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("LOGIN_RESPONSE"));


        // Check response
        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            login.put("login", true);
            JsonObject userJson = response.getAsJsonObject("data");
            int id = userJson.get("id").getAsInt();
            String role = userJson.get("role").getAsString();
            System.out.println("Login successful!");
            System.out.println("User ID: " + id + ", Role: " + role);

            if(role.equals("Patient")){
                User user = new User(id, email, password, role);
                appMain.user = user;

                //Request doctor data
                message.clear();
                data.clear();
                message.put("type", "REQUEST_PATIENT_BY_EMAIL");
                data.put("user_id", user.getId());
                data.put("email", user.getEmail());
                message.put("data", data);

                jsonMessage = gson.toJson(message);
                out.println(jsonMessage); // send JSON message

                // Read the response
                do {
                    response = responseQueue.take();
                } while (!response.get("type").getAsString().equals("REQUEST_PATIENT_BY_EMAIL_RESPONSE"));
                // Check response
                status = response.get("status").getAsString();
                if (status.equals("SUCCESS")) {
                    Patient patient = Patient.fromJason(response.getAsJsonObject("patient"));
                    System.out.println(patient);
                    appMain.patient = patient;
                    login.put("login", true);
                    login.put("message", "Login successful!");
                    login.put("patient", patient);
                    login.put("user", user);
                    return login;
                }
                login.put("login", false);
                login.put("message", response.get("message").getAsString());
                login.put("patient", null);
                login.put("user", null);
                return login;
            }else{
                login.put("login", false);
                login.put("message", "Unauthorized access");
                login.put("patient", null);
                login.put("user", null);
                return login;
            }
        } else {
            login.put("login", false);
            login.put("message", response.get("message").getAsString());
            login.put("patient", null);
            login.put("user", null);
            return login;
        }
    }



    public Doctor getDoctorFromPatient(int doctor_id, int patient_id, int user_id) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("doctor_id", doctor_id);
        data.put("user_id", user_id);
        data.put("patient_id", patient_id);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_DOCTOR_BY_ID");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message

        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("REQUEST_DOCTOR_BY_ID_RESPONSE"));
        Doctor doctor = null;

        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            doctor = Doctor.fromJason(response.getAsJsonObject("doctor"));
        }
        return doctor;
    }


    public static void main(String args[]) throws IOException {
        System.out.println("Starting Client...");
        //"localhost", 9009
        //Client client = new Client("localhost", 9009, new Application());
        //client.stopClient();
    }

    private static void releaseResources(PrintWriter printWriter, BufferedReader in, Socket socket) {
        printWriter.close();
        try{
            in.close();
        }catch(IOException e){
            System.out.println("Error closing resources");
        }

        try {
            socket.close();
            System.out.println("Connection closed");
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }
    }
}

