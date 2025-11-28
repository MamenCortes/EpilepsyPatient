package network;

import Events.ServerDisconnectedEvent;
import Events.UIEventBus;
import com.google.gson.*;
import pojos.*;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import encryption.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
    private KeyPair clientKeyPair;
    private PublicKey serverPublicKey;
    private SecretKey token;
    private final CountDownLatch tokenReady = new CountDownLatch(1);

    public Client(){
        //this.appMain = appMain;
        //generates the public and private key pair
        try {
            this.clientKeyPair = RSAKeyManager.generateKeyPair();
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
            System.out.println("This is the Client's Key Pair: ");
            System.out.println("Public Key (Base64): " + Base64.getEncoder().encodeToString(clientKeyPair.getPublic().getEncoded()));
            System.out.println("Private Key (Base64): " + Base64.getEncoder().encodeToString(clientKeyPair.getPrivate().getEncoded()));sendPublicKey(); //TODO: Should I send it with the token or separated?
            System.out.println("🔍 JVM identity hash: " + System.identityHashCode(ClassLoader.getSystemClassLoader()));
            sendTokenRequest();

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
                    JsonObject request = gson.fromJson(line, JsonObject.class); //transforms the line into a Json Object

                    String type = request.get("type").getAsString();
                    System.out.println("\n📩 Received message type: " + type);

                    if (token == null) {
                        switch (type) {
                            case "SERVER_PUBLIC_KEY": {
                                //Receive and store server's public key
                                try {
                                    String serverPublicKeyEncoded = request.get("data").getAsString();
                                    byte[] keyBytes = Base64.getDecoder().decode(serverPublicKeyEncoded);
                                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                    this.serverPublicKey = keyFactory.generatePublic(keySpec);
                                    System.out.println("Server Public Key stored successfully: "+Base64.getEncoder().encodeToString(this.serverPublicKey.getEncoded()));
                                } catch (Exception e) {
                                    System.out.println("Failed to process SERVER_PUBLIC_KEY request: " + e.getMessage());
                                    stopClient(true);
                                }
                                break;
                            }
                            case "TOKEN_REQUEST_RESPONSE": {
                                try {
                                    String encryptedToken = request.get("token").getAsString();
                                    String signatureBase64 = request.get("signature").getAsString();
                                    byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
                                    //Decrypt token with Client Private Key
                                    String token = RSAUtil.decrypt(encryptedToken, clientKeyPair.getPrivate());
                                    byte[] tokenBytes = Base64.getDecoder().decode(token);
                                    // Verify signature using Server Public Key
                                    Signature signature = Signature.getInstance("SHA256withRSA");
                                    signature.initVerify(serverPublicKey);
                                    signature.update(tokenBytes);

                                    boolean verified = signature.verify(signatureBytes);
                                    if (verified) {
                                        System.out.println("Token verified and trusted");
                                        this.token = new SecretKeySpec(tokenBytes, 0, tokenBytes.length, "AES");
                                        System.out.println("🔑 Server's AES Token (Base64): " + Base64.getEncoder().encodeToString(this.token.getEncoded()));
                                        tokenReady.countDown(); //signal login can proceed
                                    } else {
                                        System.out.println("Signature verification failed. Do not trust the token.");
                                        //This ensures that if the token is not received, the connection STOPS
                                        stopClient(true);
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error handling TOKEN_RESPONSE: " + e.getMessage());
                                    e.printStackTrace();
                                    stopClient(true);
                                }
                                break;
                            }
                        }
                    }

                    // After the Client receives the Server's Public key to encrypt the Secret key, reads responses
                    String typeDecrypted = type;
                    JsonObject decryptedRequest = request;
                    if (typeDecrypted.equals("ENCRYPTED")){
                        String encryptedData = request.get("data").getAsString();
                        String decryptedJson = AESUtil.decrypt(encryptedData, token);
                        System.out.println("This is the decrypted json: "+decryptedJson);
                        decryptedRequest = gson.fromJson(decryptedJson,JsonObject.class);
                        typeDecrypted = decryptedRequest.get("type").getAsString();
                    }
                    System.out.println("\nThis is the decrypted type received in Client: "+typeDecrypted);

                    if (typeDecrypted.equals("STOP_CLIENT")) {
                        System.out.println("Server requested shutdown");
                        stopClient(false);
                        break;
                    }

                    try {
                        responseQueue.put(decryptedRequest); //TODO: this is where it is doing everything
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //listener.setDaemon(true); // client ends even if thread is running
        listener.start();
    }

    private void sendTokenRequest(){
        JsonObject tokenRequest = new JsonObject();
        tokenRequest.addProperty("type", "TOKEN_REQUEST");
        out.println(gson.toJson(tokenRequest));
        out.flush();

        System.out.println("TOKEN_REQUEST sent to the Server");
    }

    public void sendPublicKey() {
        String clientPublicKey = Base64.getEncoder().encodeToString(clientKeyPair.getPublic().getEncoded());
        JsonObject response = new JsonObject();
        response.addProperty("type", "CLIENT_PUBLIC_KEY");
        response.addProperty("data", clientPublicKey);
        out.println(gson.toJson(response));
        out.flush();

        System.out.println("Sent Client's Public Key to Server");
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
            System.out.println("\nBefore encryption, STOP_CLIENT to Server: "+jsonMessage);
            sendEncrypted(jsonMessage, out, token);
            // TODO: ENCRYPTED
            //out.println(jsonMessage);
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
        //TODO: ENCRYPTED
        System.out.println("\nBefore encryption, LOGIN_REQUEST to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, token); // send JSON message

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
            appData.setUser(user);

            //Request doctor data
            message.clear();
            data.clear();
            message.put("type", "REQUEST_PATIENT_BY_EMAIL");
            data.put("user_id", user.getId());
            data.put("email", user.getEmail());
            message.put("data", data);

            jsonMessage = gson.toJson(message);
            System.out.println("\nBefore encryption, REQUEST_PATIENT_BY_EMAIL to Server: "+jsonMessage);
            //TODO: ENCRYPTED
            sendEncrypted(jsonMessage, out, token);
            //out.println(jsonMessage); // send JSON message

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
        //TODO: ENCRYPTED
        System.out.println("\n Before encryption REQUEST_DOCTOR_BY_ID message to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, token);

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

    public boolean sendJsonToServer(int patientId, int samplingFrequency, LocalDateTime timestamp, String filename, String base64Zip) throws Exception {

        // 1. Construir JSON raíz
        JsonObject root = new JsonObject();
        root.addProperty("type", "UPLOAD_SIGNAL");
        root.addProperty("filename", filename);

        // 2. Metadata
        JsonObject metadata = new JsonObject();
        metadata.addProperty("patient_id", patientId);
        metadata.addProperty("sampling_rate", samplingFrequency);
        metadata.addProperty("timestamp", timestamp.toString());

        root.add("metadata", metadata);

        // 3. Datos (ZIP Base64)
        root.addProperty("dataBytes", base64Zip);

        String json = gson.toJson(root);
        System.out.println("➡ Sending JSON:");
        System.out.println(json);

        // 4. Enviar al servidor
        out.println(json);
        out.flush();

        // 5. Esperar la respuesta del servidor
        JsonObject response;
        do {
            response = responseQueue.take();
            System.out.println("⬅ Received: " + response);
        } while (!response.get("type").getAsString().equals("UPLOAD_SIGNAL_RESPONSE"));

        // 6. Procesar respuesta
        String status = response.get("status").getAsString();
        if (status.equals("ERROR")) {
            throw new ServerError(response.get("message").getAsString());
        }

        return true;
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
        //TODO: ENCRYPTION
        sendEncrypted(jsonMessage, out, token);

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

    public void changePassword(String email, String newPassword) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("new_password", newPassword);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "CHANGE_PASSWORD_REQUEST");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        sendEncrypted(jsonMessage, out, token);

        //Waits for a response of type CHANGE_PASSWORD_RESPONSE
        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("CHANGE_PASSWORD_REQUEST_RESPONSE"));

        // Check response
        String status = response.get("status").getAsString();
        if (!status.equals("SUCCESS")) {
           throw new IOException("Password change failed: "+response.get("message").getAsString());
        }

        System.out.println("Password successfully changed!");

    }

    private void sendEncrypted(String message, PrintWriter out, SecretKey AESkey){
        try{
            String encryptedJson = AESUtil.encrypt(message, AESkey);
            JsonObject wrapper = new JsonObject();

            //TODO: ver si realmente el type debería ser especifico para cada case o no
            wrapper.addProperty("type", "ENCRYPTED");
            wrapper.addProperty("data", encryptedJson);

            System.out.println("\nThis is the encrypted message sent to Server :"+wrapper);

            out.println(wrapper);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendAlertToAdmin(Patient patient) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("patient", patient.toJson());

            Map<String, Object> message = new HashMap<>();
            message.put("type", "ALERT_ADMIN");
            message.put("data", data);

            String jsonMessage = gson.toJson(message);
            System.out.println("Sending alert to admin: " + jsonMessage);
            out.println(jsonMessage); // send JSON message
        } catch (Exception e) {
            System.out.println("Error sending alert to admin: " + e.getMessage());
        }
    }
}

