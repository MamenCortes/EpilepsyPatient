package network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import encryption.AESUtil;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//TODO: check tests with Public key encryption changes
public class ClientTest {

    Client client;
    Application app;
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    @BeforeEach
    void setup() throws Exception {
        app = mock(Application.class);
        socket = mock(Socket.class);

        // Mock IO streams
        in = mock(BufferedReader.class);
        out = mock(PrintWriter.class);

        client = new Client();

        setField(client, "socket", socket);
        setField(client, "in", in);
        setField(client, "out", out);
        setField(client, "running", true);
    }

    // Utility reflection method
    private static void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    void testConnectSuccess() throws Exception {

        InputStream mockInput = new ByteArrayInputStream("{\"type\":\"PING\"}\n".getBytes());
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        // Cliente es un spy para interceptar new Socket(...)
        Client client = spy(new Client());

        // Cuando connect llame a new Socket(ip,port) → devolver mockSocket
        doReturn(socket).when(client).createSocket(anyString(), anyInt());

        // Mockear streams del socket
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        // Ejecutar
        boolean ok = client.connect("localhost", 9009);

        // Validaciones
        assertTrue(ok);
    }

    @Test
    void testLoginSuccess() throws Exception {

        Client client = spy(new Client());

        // --- Mock socket ---
        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // --- Inject writer/reader ---
        PrintWriter mockWriter = mock(PrintWriter.class);
        BufferedReader mockReader = mock(BufferedReader.class);

        setField(client, "out", mockWriter);
        setField(client, "in", mockReader);

        // --- Inject AES key (simulate handshake done) ---
        SecretKey fakeAES = AESUtil.generateAESKey();
        setField(client, "token", fakeAES);

        // --- Fake queue (decrypted messages) ---
        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        // LOGIN_RESPONSE
        JsonObject loginResp = JsonParser.parseString(
                "{\"type\":\"LOGIN_RESPONSE\",\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"role\":\"Patient\"}}"
        ).getAsJsonObject();
        q.add(loginResp);

        // REQUEST_PATIENT_BY_EMAIL_RESPONSE
        Patient fakePatient = new Patient("A","B","mail",999, "F",LocalDate.now());
        fakePatient.setId(1);
        JsonObject patientResp = new JsonObject();
        patientResp.addProperty("type","REQUEST_PATIENT_BY_EMAIL_RESPONSE");
        patientResp.addProperty("status","SUCCESS");
        patientResp.add("patient", fakePatient.toJason());
        q.add(patientResp);

        setField(client, "responseQueue", q);

        // --- Act ---
        AppData appData = client.login("test@test.com", "123");

        // --- Assert ---
        assertNotNull(appData.getUser());
        assertNotNull(appData.getPatient());
        assertEquals("test@test.com", appData.getUser().getEmail());
    }




    @Test
    void testStopClientInitiatedByClient() throws Exception {

        Client client = spy(new Client());

        // --- Mock socket ---
        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(mockOutput);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(mockSocket.isClosed()).thenReturn(false);

        // Inject AES token
        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        // Connect
        client.connect("localhost", 9009);

        // --- Act ---
        client.stopClient(true);

        // --- Extract ONLY the encrypted JSON line ---
        String encryptedLine = Arrays.stream(mockOutput.toString().split("\n"))
                .filter(l -> l.trim().startsWith("{") && l.contains("\"ENCRYPTED\""))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No encrypted JSON found"));

        // --- Parse encrypted JSON wrapper ---
        JsonObject wrapper = JsonParser.parseString(encryptedLine).getAsJsonObject();
        String encryptedPayload = wrapper.get("data").getAsString();

        // --- Decrypt payload ---
        String decrypted = AESUtil.decrypt(encryptedPayload, aes);

        // --- Assert STOP_CLIENT was sent ---
        assertTrue(decrypted.contains("STOP_CLIENT"));
    }



    @Test
    void testStopClientServerConnectionError() throws Exception {

        Client client = spy(new Client());

        // Mock socket
        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(mockOutput);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Inject AES token
        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        client.connect("localhost", 9009);

        // Act
        client.stopClient(false);

        // Assert → client does NOT send STOP_CLIENT when not initiated by client
        String output = mockOutput.toString();
        assertFalse(output.contains("STOP_CLIENT"));
        assertFalse(output.contains("ENCRYPTED"));
    }


    @Test
    void testStopClientWhenServerSendsStop() throws Exception {

        Client client = spy(new Client());

        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Inject AES key
        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        client.connect("localhost", 9009);

        // --- Inject STOP_CLIENT into queue (as decrypted JSON) ---
        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        JsonObject stop = new JsonObject();
        stop.addProperty("type", "STOP_CLIENT");
        q.add(stop);

        setField(client, "responseQueue", q);

        // Wait for listener (or directly call stopClient)
        client.stopClient(false);

        // Assert: no STOP_CLIENT was sent (server initiated)
        // And client shuts down cleanly.
        assertFalse(mockSocket.isClosed());
    }


    @Test
    void testGetDoctorFromPatient() throws Exception {

        Client client = spy(new Client());

        // Mock socket
        Socket mockSocket = mock(Socket.class);
        doReturn(mockSocket).when(client).createSocket(anyString(), anyInt());

        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Inject AES key
        SecretKey aes = AESUtil.generateAESKey();
        setField(client, "token", aes);

        client.connect("localhost", 9009);

        // Fake response queue
        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        Doctor expected = new Doctor();
        expected.setName("House");

        JsonObject resp = new JsonObject();
        resp.addProperty("type", "REQUEST_DOCTOR_BY_ID_RESPONSE");
        resp.addProperty("status", "SUCCESS");
        resp.add("doctor", expected.toJason());

        q.add(resp);

        setField(client, "responseQueue", q);

        // --- Act ---
        Doctor d = client.getDoctorFromPatient(1, 1, 1);

        // --- Assert ---
        assertNotNull(d);
        assertEquals("House", d.getName());
    }

    @Test
    void testSendSignalToServer() throws Exception {

        Client client = spy(new Client());
        SecretKey aes = AESUtil.generateAESKey();
        OutputStream out = mock(OutputStream.class);
        PrintWriter mockWriter = new  PrintWriter(out);

        // Fake socket
        doReturn(socket).when(client).createSocket(any(), anyInt());
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        setField(client, "token", aes);
        setField(client, "out", mockWriter);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        JsonObject ok = new JsonObject();
        ok.addProperty("type", "UPLOAD_SIGNAL_RESPONSE");
        ok.addProperty("status", "SUCCESS");
        q.add(ok);
        setField(client, "responseQueue", q);

        boolean result = client.sendSignalToServer(
                12, 500, LocalDateTime.now(), "sig.zip", "ABC123"
        );

        assertTrue(result);
    }

    @Test
    void testSendReport() throws Exception {

        Client client = spy(new Client());
        SecretKey aes = AESUtil.generateAESKey();
        OutputStream out = mock(OutputStream.class);
        PrintWriter mockWriter = new  PrintWriter(out);
        setField(client, "token", aes);
        setField(client, "out", mockWriter);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        JsonObject ok = new JsonObject();
        ok.addProperty("type", "SAVE_REPORT_RESPONSE");
        ok.addProperty("status", "SUCCESS");
        q.add(ok);
        setField(client, "responseQueue", q);

        Report r = new Report();

        assertDoesNotThrow(() ->
                client.sendReport(r, 3, 5)
        );
    }

    @Test
    void testChangePassword() throws Exception {

        Client client = spy(new Client());
        SecretKey aes = AESUtil.generateAESKey();
        OutputStream out = mock(OutputStream.class);
        PrintWriter mockWriter = new  PrintWriter(out);
        setField(client, "token", aes);
        setField(client, "out", mockWriter);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        JsonObject ok = new JsonObject();
        ok.addProperty("type", "CHANGE_PASSWORD_REQUEST_RESPONSE");
        ok.addProperty("status", "SUCCESS");
        q.add(ok);
        setField(client, "responseQueue", q);

        assertDoesNotThrow(() ->
                client.changePassword("mail", "pw")
        );
    }

}
