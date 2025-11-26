package network;

import Events.ServerDisconnectedEvent;
import Events.UIEventBus;
import com.google.gson.*;
import pojos.*;
import ui.windows.Application;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import encryption.*;

import javax.crypto.SecretKey;

public class Client {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    //private Application appMain;
    private Gson gson = new Gson();
    private volatile Boolean running;
    //Estructura diseñada para comunicar threads entre sí de manera segura y sincronizada
    //permite que un thread meta mensajes en la cola (con put())
    //Y que otro thread los reciba (con take() o poll())
    //si no hay mensajes, take() se bloquea automáticamente, sin consumir CPU
    private BlockingQueue<JsonObject> responseQueue = new LinkedBlockingQueue<>();
    private KeyPair keyPair;
    private PublicKey serverPublicKey;
    private SecretKey AESkey;

    public Client(){
        //this.appMain = appMain;
        //generates the public and private key pair
        try {
            this.keyPair = RSAKeyManager.generateKeyPair();
        }catch (Exception ex){
            System.out.println("Error generating key pair: "+ex.getMessage());
        }
    }

    public Boolean connect(String ip, int port) {

        try {
            //socket = new Socket("localhost", 9009);
            socket = createSocket(ip, port);
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

    protected Socket createSocket(String ip, int port) throws IOException {
        return new Socket(ip, port);
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

                    // Store the server's public key
                    if (type.equals("SERVER_PUBLIC_KEY")){
                        String keyEncoded = json.get("data").getAsString();
                        byte[] keyBytes = Base64.getDecoder().decode(keyEncoded);
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                        try{
                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                            PublicKey serverPublicKey = keyFactory.generatePublic(keySpec);
                            this.serverPublicKey = serverPublicKey;

                            // Generate the AES temporary key
                            this.AESkey = AESUtil.generateAESKey();
                            System.out.println("AES key generated");

                            // Encrypt AES key with the server's public key
                            String encryptedAESKey = RSAUtil.encrypt(Base64.getEncoder().encodeToString(AESkey.getEncoded()), serverPublicKey);

                            // Send the encrypted AES key to the server
                            JsonObject AESkeyJson = new JsonObject();
                            AESkeyJson.addProperty("type", "CLIENT_AES_KEY");
                            AESkeyJson.addProperty("data", encryptedAESKey);
                            out.println(gson.toJson(AESkeyJson));
                            System.out.println("This is the Server's shared public RSA key: "+Base64.getEncoder().encodeToString(serverPublicKey.getEncoded()));
                            System.out.println("This is the shared secret AES key: "+Base64.getEncoder().encodeToString(AESkey.getEncoded()));
                            out.flush();


                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println("Failed to fetch server public key");
                        }

                        continue;

                    }

                    if (type.equals("STOP_CLIENT")) {
                        System.out.println("Server requested shutdown");
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
                //In case the connection is closed without the server asking for it first
                stopClient(false);
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

    /**
     * Sends messages as an introduction to the established connection to the Server.
     *
     * @throws IOException
     */
    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        out.println("Hi! I'm a new client!\n");
    }

    /**
     *
     * @param initiatedByClient
     */
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
            UIEventBus.BUS.post(new ServerDisconnectedEvent());
            //appMain.onServerDisconnected();
        }

        releaseResources(out, in, socket);
    }

    /**
     * Sends a login request to the server provided the email and the password of a patient in the network. Then,
     * handles the server's response and if successful, it loads additional patient data.
     * <p>
     *     Creates a Map to represent the login data and wraps it into a JSON-like map with a type and data fields.
     *     It converts the whole Map into a JSON using Gson. Finally it sends the Json object over the output stream.
     *     Waits for a response and gets the status response: either SUCCESS or ERROR.
     *     If successful, it gets the user's id and role and creates the {@code User} object and
     *     stores it. Then adds additional information about the patient by request.
     * </p>
     *
     * @param email         The patient's email
     * @param password      The patient's password
     * @throws IOException
     * @throws InterruptedException
     * @throws LogInError
     *
     * @see Gson
     */
    public AppData login(String email, String password) throws IOException, InterruptedException, LogInError {
        //String message = "LOGIN;" + email + ";" + password;
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("access_permits", "Patient");

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOGIN_REQUEST");
        message.put("data", data); //JSON-like map

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message
        System.out.println(jsonMessage);

        //Waits for a response of type LOGIN_RESPONSE
        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("LOGIN_RESPONSE"));


        AppData appData = new AppData();
        // Check response
        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            JsonObject userJson = response.getAsJsonObject("data");
            int id = userJson.get("id").getAsInt();
            String role = userJson.get("role").getAsString();
            System.out.println("Login successful!");
            System.out.println("User ID: " + id + ", Role: " + role);

            User user = new User(id, email, password, role);
            //appMain.user = user; //TODO; eliminar
            appData.setUser(user);

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
            if (!status.equals("SUCCESS")) {
                throw new LogInError(response.get("message").getAsString());
            }

            Patient patient = Patient.fromJason(response.getAsJsonObject("patient"));
            System.out.println(patient);
            //appMain.patient = patient; //TODO: eliminar
            appData.setPatient(patient);
            return appData;
        } else {
            throw new LogInError(response.get("message").getAsString());
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

    public void sendJsonToServer(String json) throws Exception {
        out.println(json);
        out.flush();

    }
    private static void releaseResources(PrintWriter printWriter, BufferedReader in,Socket socket) {
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

    public void sendReport(Report report, int patient_id, int user_id) throws IOException, InterruptedException, ServerError {
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", user_id);
        data.put("patient_id", patient_id);
        data.put("report", report.toJson());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "SAVE_REPORT");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message

        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("SAVE_REPORT_RESPONSE"));
        Doctor doctor = null;

        String status = response.get("status").getAsString();
        if (status.equals("ERROR")) {
            throw new ServerError(response.get("message").getAsString());
        }
    }

}

