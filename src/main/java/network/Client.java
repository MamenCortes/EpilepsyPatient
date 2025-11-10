package network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pojos.Doctor;
import pojos.Patient;
import pojos.User;
import ui.windows.Application;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    private Application appMain;
    private Gson gson = new Gson();

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
            sendInitialMessage();
            return true;
        }catch(IOException e){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }

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

    //TODO: change to send Exception instead of MAP
    public Map<String, Object> login(String email, String password) throws IOException {
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
        String responseLine = in.readLine();
        JsonObject response = gson.fromJson(responseLine, JsonObject.class);

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
                responseLine = in.readLine();
                response = gson.fromJson(responseLine, JsonObject.class);
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

    public void stopClient() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STOP_CLIENT");
        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage);
        System.out.println("Sent: " + jsonMessage);
        //out.println("stop");
        releaseResources(out, socket);
    }

    public Doctor getDoctorFromPatient(int doctor_id, int patient_id, int user_id) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("doctor_id", doctor_id);
        data.put("user_id", user_id);
        data.put("patient_id", patient_id);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_DOCTOR_BY_ID");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message

        String line = in.readLine();
        JsonObject response = gson.fromJson(line, JsonObject.class);
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

    private static void releaseResources(PrintWriter printWriter, Socket socket) {
        printWriter.close();

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }
    }

}

