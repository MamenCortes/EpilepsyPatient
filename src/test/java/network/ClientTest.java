package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import encryption.AESUtil;
import encryption.RSAKeyManager;
import org.junit.jupiter.api.*;
import pojos.AppData;
import pojos.Doctor;
import pojos.Patient;
import pojos.Report;
import ui.windows.Application;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.crypto.SecretKey;

import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//TODO: check tests with Public key encryption changes
public class ClientTest {

    Client client;
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    // Reflection helper
    private static void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @BeforeEach
    void setup() throws Exception {
        client = new Client();
        socket = mock(Socket.class);
        in = mock(BufferedReader.class);
        out = mock(PrintWriter.class);

        setField(client, "socket", socket);
        setField(client, "in", in);
        setField(client, "out", out);
        setField(client, "running", true);
    }

    // -------------------------------------------------------------------------
    @Test
    void testConnectSuccess() throws Exception {

        InputStream mockInput = new ByteArrayInputStream("{\"type\":\"PING\"}\n".getBytes());
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        boolean ok = client.connect("localhost", 9009);

        assertTrue(ok);
    }

    // -------------------------------------------------------------------------
    @Test
    void testLoginSuccess() throws Exception {

        Client client = spy(new Client());

        // --- Mock socket ---
        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        PrintWriter mockWriter = mock(PrintWriter.class);
        BufferedReader mockReader = mock(BufferedReader.class);

        setField(client, "out", mockWriter);
        setField(client, "in", mockReader);

        // --- Store RSA keys for this user ---
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        RSAKeyManager.saveKey(pair, "test_test_com");

        // --- Inject AES token (skip handshake) ---
        SecretKey fakeAES = AESUtil.generateAESKey();
        client.saveToken(fakeAES);

        // --- Fake decrypted queue ---
        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        // LOGIN_RESPONSE
        JsonObject loginResp = JsonParser.parseString(
                "{\"type\":\"LOGIN_RESPONSE\",\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"role\":\"Patient\"}}"
        ).getAsJsonObject();
        q.add(loginResp);

        // ---- CORRECT Patient JSON (must match Patient.fromJason) ----
        JsonObject patientJson = new JsonObject();
        patientJson.addProperty("id", 1);
        patientJson.addProperty("name", "A");
        patientJson.addProperty("surname", "B");
        patientJson.addProperty("email", "test@test.com");
        patientJson.addProperty("contact", 999);
        patientJson.addProperty("dateOfBirth", LocalDate.now().toString());  // correct name
        patientJson.addProperty("gender", "F");
        patientJson.addProperty("doctorId", 5);  // required

        // optional fields
        patientJson.add("signals", new JsonArray());
        patientJson.add("reports", new JsonArray());

        JsonObject patientResp = new JsonObject();
        patientResp.addProperty("type","REQUEST_PATIENT_BY_EMAIL_RESPONSE");
        patientResp.addProperty("status","SUCCESS");
        patientResp.add("patient", patientJson);

        q.add(patientResp);
        setField(client, "responseQueue", q);

        // --- Act ---
        AppData appData = client.login("test@test.com", "123");

        // --- Assert ---
        assertNotNull(appData.getUser());
        assertNotNull(appData.getPatient());
        assertEquals("test@test.com", appData.getUser().getEmail());
    }


    // -------------------------------------------------------------------------
    @Test
    void testStopClientInitiatedByClient() throws Exception {

        Client client = spy(new Client());
        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(mockOutput);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(mockSocket.isClosed()).thenReturn(false);

        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        client.connect("localhost", 9009);

        client.stopClient(true);

        String encryptedLine = Arrays.stream(mockOutput.toString().split("\n"))
                .filter(l -> l.trim().startsWith("{") && l.contains("\"ENCRYPTED\""))
                .findFirst()
                .orElseThrow();

        JsonObject wrapper = JsonParser.parseString(encryptedLine).getAsJsonObject();
        String encryptedPayload = wrapper.get("data").getAsString();
        String decrypted = AESUtil.decrypt(encryptedPayload, aes);

        assertTrue(decrypted.contains("STOP_CLIENT"));
    }

    // -------------------------------------------------------------------------
    @Test
    void testStopClientServerConnectionError() throws Exception {

        Client client = spy(new Client());

        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(mockOutput);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        client.connect("localhost", 9009);

        client.stopClient(false);

        String out = mockOutput.toString();
        assertFalse(out.contains("STOP_CLIENT"));
        assertFalse(out.contains("ENCRYPTED"));
    }

    // -------------------------------------------------------------------------
    @Test
    void testStopClientWhenServerSendsStop() throws Exception {

        Client client = spy(new Client());
        Socket mockSocket = mock(Socket.class);

        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(mockSocket.isClosed()).thenReturn(false);

        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        client.connect("localhost", 9009);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        JsonObject stop = new JsonObject();
        stop.addProperty("type", "STOP_CLIENT");
        q.add(stop);
        setField(client, "responseQueue", q);

        client.stopClient(false);

        assertFalse(mockSocket.isClosed());
    }

    // -------------------------------------------------------------------------
    @Test
    void testGetDoctorFromPatient() throws Exception {

        Client client = spy(new Client());
        Socket mockSocket = mock(Socket.class);

        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        client.connect("localhost", 9009);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        Doctor d = new Doctor("House", "MD", "mail", 888, "Neuro");
        d.setId(1);

        JsonObject resp = new JsonObject();
        resp.addProperty("type","REQUEST_DOCTOR_BY_ID_RESPONSE");
        resp.addProperty("status","SUCCESS");
        resp.add("doctor", d.toJason());
        q.add(resp);

        setField(client, "responseQueue", q);

        Doctor result = client.getDoctorFromPatient(1, 1, 1);

        assertNotNull(result);
        assertEquals("House", result.getName());
    }

    // -------------------------------------------------------------------------
    @Test
    void testSendSignalToServer() throws Exception {

        Client client = spy(new Client());
        SecretKey aes = AESUtil.generateAESKey();

        OutputStream out = mock(OutputStream.class);
        PrintWriter mockWriter = new PrintWriter(out);

        doReturn(socket).when(client).createSocket(any(), anyInt());
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        setField(client, "token", aes);
        setField(client, "out", mockWriter);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        JsonObject ok = new JsonObject();
        ok.addProperty("type","UPLOAD_SIGNAL_RESPONSE");
        ok.addProperty("status","SUCCESS");
        q.add(ok);
        setField(client, "responseQueue", q);

        boolean result =
                client.sendSignalToServer(12, 500, LocalDateTime.now(),
                        "sig.zip", "ABC123");

        assertTrue(result);
    }

    // -------------------------------------------------------------------------
    @Test
    void testSendReport() throws Exception {

        Client client = spy(new Client());
        SecretKey aes = AESUtil.generateAESKey();

        OutputStream out = mock(OutputStream.class);
        PrintWriter mockWriter = new PrintWriter(out);

        setField(client, "token", aes);
        setField(client, "out", mockWriter);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        JsonObject ok = new JsonObject();
        ok.addProperty("type","SAVE_REPORT_RESPONSE");
        ok.addProperty("status","SUCCESS");
        q.add(ok);
        setField(client, "responseQueue", q);

        Report r = new Report();

        assertDoesNotThrow(() -> client.sendReport(r, 3, 5));
    }

    // -------------------------------------------------------------------------
    @Test
    void testChangePassword() throws Exception {

        Client client = spy(new Client());
        SecretKey aes = AESUtil.generateAESKey();

        // RSA keypair required
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        String file = "mail";
        RSAKeyManager.saveKey(pair, file);

        OutputStream out = mock(OutputStream.class);
        PrintWriter mockWriter = new PrintWriter(out);

        setField(client, "token", aes);
        setField(client, "out", mockWriter);

        // Fake server public key for signature verification
        setField(client, "serverPublicKey", pair.getPublic());

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        JsonObject ok = new JsonObject();
        ok.addProperty("type","CHANGE_PASSWORD_REQUEST_RESPONSE");
        ok.addProperty("status","SUCCESS");
        q.add(ok);
        setField(client, "responseQueue", q);

        assertDoesNotThrow(() -> client.changePassword("mail", "pw"));
    }
}
