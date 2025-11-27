package network;

import Events.ServerDisconnectedEvent;
import Events.ShowHelpDialogEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import pojos.Doctor;
import pojos.Patient;
import ui.windows.Application;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//TODO: check tests with events
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

        // Fake socket streams
        Patient p = new Patient();
        ByteArrayInputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"LOGIN_RESPONSE\",\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"role\":\"Patient\"}}\n".getBytes()
        );
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        // Start connection
        client.connect("localhost", 9009);

        // Fill queue manually simulating server
        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENT_BY_EMAIL_RESPONSE");
        response.addProperty("status", "SUCCESS");
        response.add("patient", p.toJason());
        q.add(JsonParser.parseString(
                "{\"type\":\"LOGIN_RESPONSE\",\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"role\":\"Patient\"}}"
        ).getAsJsonObject());
        q.add(response);

        setField(client, "responseQueue", q);

        client.login("test@test.com", "123");

        assertNotNull(app.user);
        assertNotNull(app.patient);
    }


    @Test
    void testStopClientInitiatedByClient() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        // Mock socket and out
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(socket.isClosed()).thenReturn(false);

        client.connect("localhost", 9009);

        client.stopClient(true);  // client-requested shutdown

        String sent = mockOutput.toString();
        assertTrue(sent.contains("STOP_CLIENT"));
        verify(app, never()).onServerDisconnected(new ServerDisconnectedEvent());   // UI must NOT be notified
    }

    @Test
    void testStopClientServerConnectionError() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        client.connect("localhost", 9009);

        client.stopClient(false);  // server-side shutdown

        String sent = mockOutput.toString();
        assertFalse(sent.contains("STOP_CLIENT"));     // not client-initiated
        verify(app).onServerDisconnected(new ServerDisconnectedEvent());            // UI must be informed
    }

    @Test
    void testStopClientWhenServerSendsStop() throws Exception {

        ByteArrayInputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"STOP_CLIENT\"}\n".getBytes()
        );
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        client.connect("localhost", 9009);

        // Give the listener time to process the STOP_CLIENT
        Thread.sleep(100);

        verify(app).onServerDisconnected(new ServerDisconnectedEvent());     // UI notified
    }

    @Test
    void testGetDoctorFromPatient() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        client.connect("localhost", 9009);

        Doctor sendDoctor = new Doctor();
        sendDoctor.setName("House");
        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        q.add(JsonParser.parseString(
                "{\"type\":\"REQUEST_DOCTOR_BY_ID_RESPONSE\",\"status\":\"SUCCESS\",\"doctor\":"+sendDoctor.toJason()+"}"
        ).getAsJsonObject());

        setField(client, "responseQueue", q);

        Doctor d = client.getDoctorFromPatient(1, 1, 1);

        assertNotNull(d);
        assertEquals("House", d.getName());
    }



}
