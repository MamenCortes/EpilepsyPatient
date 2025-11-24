package ui.windows;

import com.google.gson.JsonObject;
import net.miginfocom.swing.MigLayout;
import BITalino.SignalRecorderService;
import ui.components.MyButton;
import ui.components.MyTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Base64;

//TODO: al parar de grabar que pregunte si estás seguro
//TODO: feedback sobre estableciendo la conexión y mandando datos
//TODO: si ha fallado la conexión, botón para volver a enviarlo
/**
 * Panel that manages live physiological signal recording using a BITalino device.
 * <p>
 * This workflow includes:
 * <ul>
 *     <li>Entering the BITalino MAC address</li>
 *     <li>Connecting to the device</li>
 *     <li>Starting and stopping the acquisition</li>
 *     <li>Compressing and uploading the collected data to the server</li>
 *     <li>Displaying user feedback (recording animation, upload progress, errors)</li>
 * </ul>
 * </p>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>The panel is created once inside {@link PatientMenu} and reused each time the user enters it.</li>
 *     <li>{@link #initPanel()} builds all connection and recording UI elements only once.</li>
 *     <li>The internal state is controlled by:
 *          <ul>
 *              <li>{@code recording} – TRUE when acquisition is active</li>
 *              <li>{@code saving} – TRUE during upload</li>
 *          </ul>
 *     </li>
 *     <li>After leaving the panel (Back to Menu), {@link #resetPanel()} returns it to its initial state:
 *          <ul>
 *              <li>MAC address cleared</li>
 *              <li>Recording animations reset</li>
 *              <li>Error messages hidden</li>
 *              <li>Button states restored</li>
 *          </ul>
 *     </li>
 * </ul>
 */
public class RecordSignal extends JPanel implements ActionListener {
    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", Font.ITALIC, 15);
    private final Font subHeadingFont = new Font("sansserif", 1, 20);
    private final Color contentColor = Application.dark_turquoise;
    private ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/ekg-monitor64_02.png"));
    private ImageIcon recordingGif = new ImageIcon(getClass().getResource("/icons/ecg-gif128.gif"));
    private ImageIcon recordingImg = new ImageIcon(getClass().getResource("/icons/ecg-gif128.png"));
    private ImageIcon uploadingGif = new ImageIcon(getClass().getResource("/icons/uploading128.gif"));
    private ImageIcon uploadedImg = new ImageIcon(getClass().getResource("/icons/check128.png"));
    public SignalRecorderService recorderService;
    public File lastZipFile;
    //Components
    JLabel errorMessage;
    JLabel errorMessage2;
    MyButton okButton;
    MyButton back2MenuBt;
    MyButton stopRecording;
    MyButton startRecording;
    CardLayout cardLayout;
    CardLayout buttonsLayout;
    JPanel buttonStack;
    JPanel cardPanel;
    JLabel image;
    MyTextField iptxtField;

    //
    Boolean recording = false;
    Boolean saving = false;
    Application appMain;

    public static void main(String[] args) {
        RecordSignal symptomPanel = null;
        try {
            symptomPanel = new RecordSignal(new Application());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setSize(800, 600);
        frame.setBounds(100, 100, 602, 436);
        frame.setResizable(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(symptomPanel);
    }
    /**
     * Creates the record signal panel and initializes animations, buttons and UI layouts.
     *
     * @param app reference to the main application controller
     */
    public RecordSignal(Application app) {
        appMain = app;
        initPanel();
    }
    /**
     * Initializes all UI components and layouts:
     * <ul>
     *     <li>Panel 1 — Connection to BITalino (MAC address + connect button)</li>
     *     <li>Panel 2 — Recording interface with start/stop controls and animations</li>
     *     <li>CardLayout — Enables switching between connection and recording views</li>
     *     <li>Button stack — Swaps “Start Recording”, “Stop Recording”, or hides both</li>
     * </ul>
     * <p>
     * This method is called once in the constructor; afterwards interaction
     * is handled dynamically through card layouts and SwingWorker tasks.
     * </p>
     */
    private void initPanel() {
        this.setLayout(new MigLayout("fill, inset 20, wrap 4", "[25%][25%][25%][25%]", "[15%][60%][5%][5%]"));
        this.setBackground(Color.white);
        //Add Title
        JLabel title = new JLabel("Signal Monitoring");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(icon);
        add(title, "cell 0 0 4 1, alignx left");

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel recordSignalPanel = new JPanel();
        JPanel connectBitalino = new JPanel();
        connectBitalino.setBackground(Color.white);
        connectBitalino.setLayout(new MigLayout("wrap, fill, inset 20", "push[center]push", "push[]25[]10[]20[]push"));
        JLabel label = new JLabel("Introduce server MAC Address:");
        label.setFont(subHeadingFont);
        label.setForeground(contentColor);
        connectBitalino.add(label);

        iptxtField = new MyTextField();
        iptxtField.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/pass.png")));
        iptxtField.setHint("Enter new MAC Address...");
        connectBitalino.add(iptxtField, "w 60%");

        errorMessage = new JLabel();
        errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
        errorMessage.setFont(contentFont);
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        errorMessage.setVisible(false);

        okButton = new MyButton();
        okButton.addActionListener(this);
        okButton.setText("CONNECT");
        okButton.setBackground(Application.turquoise);
        okButton.setForeground(Color.white);

        connectBitalino.add(okButton, "center, growx");
        connectBitalino.add(errorMessage, "center, growx");

        recordSignalPanel.setLayout(new MigLayout("wrap, fill, inset 20", "push[center][center]push", "push[30%][10%][70%]push"));
        recordSignalPanel.setBackground(Color.white);
        startRecording = new MyButton("Start Recording", Application.turquoise, Color.white);
        startRecording.addActionListener(this);
        stopRecording = new MyButton("Stop Recording", Application.turquoise, Color.white);
        stopRecording.addActionListener(this);
        stopRecording.setVisible(false);
        image = new JLabel(recordingImg);

        errorMessage2 = new JLabel();
        errorMessage2.setHorizontalAlignment(SwingConstants.CENTER);
        errorMessage2.setFont(contentFont);
        errorMessage2.setForeground(Color.red);
        errorMessage2.setText("Error message test");
        errorMessage2.setVisible(false);

        buttonsLayout = new CardLayout();
        buttonStack = new JPanel(buttonsLayout);
        buttonStack.setOpaque(false);
        JLabel empty = new JLabel();
        empty.setBackground(Color.white);
        buttonStack.add(startRecording, "START");
        buttonStack.add(stopRecording, "STOP");
        buttonStack.add(empty, "NULL");
        buttonsLayout.show(buttonStack, "START");

        recordSignalPanel.add(buttonStack, "cell 0 2, center, growx");

        recordSignalPanel.add(image, "cell 0 0 2 1, growx");
        recordSignalPanel.add(errorMessage2, "cell 0 1 2 1, growx");

        back2MenuBt = new MyButton();
        back2MenuBt.addActionListener(this);
        back2MenuBt.setText("BACK TO MENU");

        cardPanel.add(connectBitalino, "Panel1");
        cardPanel.add(recordSignalPanel, "Panel2");

        // Mostrar un panel:
        cardLayout.show(cardPanel, "Panel1");
        add(cardPanel, "cell 0 1 4 1, grow");
        add(back2MenuBt, "cell 0 2 4 1, center");
    }
    /**
     * Displays an error message (red) in any designated label.
     *
     * @param errorMessage   target label
     * @param message message text
     */
    private void showErrorMessage(JLabel errorMessage, String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setForeground(Color.red);
    }
    /**
     * Displays a positive feedback message (turquoise).
     *
     * @param errorMessage   target label
     * @param message message text
     */
    private void showFeedbackMessage(JLabel errorMessage, String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setForeground(contentColor);
    }
    /**
     * Handles all user actions in the panel:
     * <ul>
     *     <li><b>Connect</b>: Attempts to start the BITalino service and enters recording view.</li>
     *     <li><b>Back to Menu</b>: Resets panel state and returns to main menu.</li>
     *     <li><b>Start Recording</b>: Displays animation and prevents navigation until stopped.</li>
     *     <li><b>Stop Recording</b>: Confirms with user, stops acquisition, uploads ZIP file
     *         to server through a background task, and re-enables menu navigation.</li>
     * </ul>
     *
     * @param e triggered ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okButton){
            showFeedbackMessage(errorMessage, "Connecting to Bitalino...");
            showFeedbackMessage(errorMessage2, "Clic start to start recording");
            String macAdd = iptxtField.getText();
            System.out.println(macAdd);
            recorderService = new SignalRecorderService(macAdd);
            recorderService.startRecording();

            if (recorderService.isRecording()) {
                cardLayout.show(cardPanel, "Panel2");
            } else {
                showFeedbackMessage(errorMessage, "Connection failed");
            }

        }else if(e.getSource() == back2MenuBt){
            cardLayout.show(cardPanel, "Panel1");
            resetPanel();
            appMain.changeToMainMenu();
        } else if (e.getSource() == startRecording) {
            if(!recording){
                image.setIcon(recordingGif);
                showFeedbackMessage(errorMessage2, "Recording...");
                recording = true;
                back2MenuBt.setVisible(false);
                buttonsLayout.show(buttonStack, "STOP");
            }
        } else if (e.getSource() == stopRecording) {
            if(recording){
                int option = JOptionPane.showConfirmDialog(this,"Are you sure you want to stop the recording?");
                if(option == JOptionPane.YES_OPTION){
                    buttonsLayout.show(buttonStack, "NULL");
                    image.setIcon(uploadingGif);
                    showFeedbackMessage(errorMessage2, "Saving recording...");
                    recording= false;
                    startSavingProcess();
                }
            }

        }
    }
    /**
     * If saving fails, this dialog allows the user to retry the upload process.
     */
    private void askRetry() {
        int option = JOptionPane.showConfirmDialog(
                null,
                "Error saving the signal.\n¿Do you want to retry?",
                "Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            startSavingProcess();  //vuelve a ejecutar el proceso
        }else if (option == JOptionPane.NO_OPTION) {
            image.setIcon(null);
            showErrorMessage(errorMessage2, "Error saving signal");
            back2MenuBt.setVisible(true);
            buttonsLayout.show(buttonStack, "START");
            recording = false;
        }
    }
    /**
     * Starts the asynchronous saving process:
     * <ul>
     *     <li>Stops the BITalino recorder</li>
     *     <li>Retrieves the generated ZIP containing signal samples</li>
     *     <li>Encodes ZIP as Base64</li>
     *     <li>Sends it to the server with metadata (patient ID, sampling rate, timestamp)</li>
     *     <li>Updates the UI with success or error animations/messages</li>
     * </ul>
     * This work is done inside a SwingWorker to avoid blocking the UI thread.
     */
    private void startSavingProcess() {
        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {
                try {
                    recorderService.stopRecording();
                    lastZipFile = recorderService.getZipFile();
                    JsonObject request = new JsonObject();
                    request.addProperty("type", "UPLOAD_SIGNAL");
                    JsonObject metadata = new JsonObject();
                    metadata.addProperty("patient_id", appMain.patient.getId());
                    metadata.addProperty("sampling_rate", recorderService.getFs());
                    metadata.addProperty("date", LocalDateTime.now().toString());
                    request.add("metadata", metadata);
                    request.addProperty("compression", "zip-base64");
                    request.addProperty("filename", lastZipFile.getName());
                    byte[] zipBytes = Files.readAllBytes(lastZipFile.toPath());
                    String base64Zip = Base64.getEncoder().encodeToString(zipBytes);
                    request.addProperty("data", base64Zip);
                    System.out.println(request);
                    appMain.client.sendJsonToServer(String.valueOf(request));
                    Thread.sleep(3000);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        image.setIcon(uploadedImg);
                        showFeedbackMessage(errorMessage2, "Signal saved successfully.");
                        back2MenuBt.setVisible(true);
                        //startRecording.setVisible(true);
                        buttonsLayout.show(buttonStack, "START");
                        recording = false;
                    } else {
                        askRetry();
                    }

                } catch (Exception e) {
                    askRetry();
                }
            }
        }.execute();
    }
    /**
     * Resets all UI elements to the initial state:
     * <ul>
     *     <li>Switches to the connection panel</li>
     *     <li>Restores button visibility</li>
     *     <li>Resets animations to default image</li>
     *     <li>Clears MAC address field</li>
     *     <li>Hides error messages</li>
     * </ul>
     */
    private void resetPanel() {
        cardLayout.show(cardPanel, "Panel1");
        buttonsLayout.show(buttonStack, "START");
        image.setIcon(recordingImg);
        errorMessage.setVisible(false);
        errorMessage.setVisible(false);
        iptxtField.setText("");
    }

}

