package ui.windows;

import Events.CloseAppEvent;
import Events.RecordingFullyStoppedEvent;
import signalRecording.RecordingException;
import Events.BITalinoDisconnectedEvent;
import Events.UIEventBus;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import signalRecording.SignalRecorderService;
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
 *
 * @author MamenCortes
 * @author MartaSanchezdelHoyo
 * @author paulablancog
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
    MyButton connectBt;
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
    private String macAdd="";

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
        recorderService = app.recorderService; //Only one instance of the recorder service
        initPanel();
        UIEventBus.BUS.register(this);

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

        connectBt = new MyButton();
        connectBt.addActionListener(this);
        connectBt.setText("CONNECT");
        connectBt.setBackground(Application.turquoise);
        connectBt.setForeground(Color.white);

        connectBitalino.add(connectBt, "center, w 60%");
        connectBitalino.add(errorMessage, "center, growx");

        recordSignalPanel.setLayout(new MigLayout("wrap, fill, inset 20, debug", "[center][center]", "push[70%][10%][20%]push"));
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

        recordSignalPanel.add(buttonStack, "cell 0 2 2 1, center, w 40%");

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
        if(e.getSource() == connectBt){
            errorMessage.setVisible(false);
            errorMessage.setText("");


            macAdd = iptxtField.getText();
            System.out.println(macAdd);

            if(macAdd.equals("") || macAdd.length() != 12){
                showErrorMessage(errorMessage, "Please enter a valid MAC Address");
                return;
            }

            showFeedbackMessage(errorMessage, "Connecting to Bitalino...");
            //cambiar a conneted and start recording panel
            //recorderService = new SignalRecorderService(macAdd);
            recorderService.setMacAddress(macAdd);
            try {
                recorderService.bitalinoConnect();
            } catch (RecordingException ex) {
                showErrorMessage(errorMessage, ex.getError().getDescription());
                return;
            }
            if (recorderService.isConnected()) {
                cardLayout.show(cardPanel, "Panel2");
            } else {
                showErrorMessage(errorMessage, "Connection failed, review MAC address and make sure BITalino is on.");
            }

        }else if(e.getSource() == back2MenuBt){
            cardLayout.show(cardPanel, "Panel1");
            resetPanel();
            try{
                recorderService.closeConnectionToBitalino();
            }catch(RecordingException ex){
                showErrorMessage(errorMessage, ex.getError().getDescription());
            }
            appMain.changeToMainMenu();
        } else if (e.getSource() == startRecording) {
            // try to reconnect if necessary
            if (!recorderService.isConnected()) {
                System.out.println("BITalino not connected");
                showErrorMessage(errorMessage2, "BITalino is not connected");
                return;
                /*try {
                    recorderService.bitalinoConnect();
                } catch (RecordingException ex) {
                    showFeedbackMessage(errorMessage2, ex.getError().getFullMessage());
                    return;
                }*/
                /*if (!recorderService.isConnected()) {
                    System.out.println("Reconnection failed.");
                    showFeedbackMessageDelayed(errorMessage2, "Reconnection failed please try again ", 1500);
                    return;
                }*/
            }

            try {
                recorderService.startRecording();
                if(!recording){
                    recording = true;
                    updateUIRecording();
                }
            } catch ( RecordingException ex) {
                showErrorMessage(errorMessage2, ex.getError().getFullMessage());
            }

        } else if (e.getSource() == stopRecording) {
            if(recording){
                int option = JOptionPane.showConfirmDialog(this,"Are you sure you want to stop the recording?");
                if(option == JOptionPane.YES_OPTION){
                    updateUISaving();
                    try {
                        recorderService.stopRecording();
                        startSavingProcess();
                    } catch (RecordingException ex) {
                        showFeedbackMessage(errorMessage2, ex.getError().getFullMessage());
                    }
                }
            }

        }
    }

    private void hideErrorMessage(JLabel errorMessage) {
        errorMessage.setVisible(false);
        errorMessage.setText("");
    }

    private void updateUIWaitingToStartRecording(){
        //recording = false;
        hideErrorMessage(errorMessage2);
        back2MenuBt.setVisible(true);
        buttonsLayout.show(buttonStack, "START");
        image.setIcon(recordingImg); // reset animation

    }

    private void updateUIRecording(){
        back2MenuBt.setVisible(false);
        image.setIcon(recordingGif);
        showFeedbackMessage(errorMessage2, "Recording...");
        buttonsLayout.show(buttonStack, "STOP");
    }

    private void updateUISaving(){
        back2MenuBt.setVisible(false);
        buttonsLayout.show(buttonStack, "NULL");
        image.setIcon(uploadingGif);
        showFeedbackMessage(errorMessage2, "Saving recording...");
    }

    private void showFeedbackMessageDelayed(JLabel label, String msg, int delayMs) {
        new javax.swing.Timer(delayMs, e -> {
            showFeedbackMessage(label, msg);
            ((javax.swing.Timer) e.getSource()).stop();
        }).start();
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
    @Subscribe
    public void onBitalinoDisconnected(BITalinoDisconnectedEvent event) {

        /*System.out.println("BITalino disconnected");
        // Only attempt upload if a partial recording exists
        if (event.isPartialRecordingAvailable()) {
            startSavingProcess();
        } else {
            showFeedbackMessage(errorMessage2,
                    "BITalino disconnected unexpectedly. No recording data available.");
            buttonsLayout.show(buttonStack, "START");
            back2MenuBt.setVisible(true);
            recording = false;
        }*/

        SwingUtilities.invokeLater(() -> {

            System.out.println("Bitalino disconnected");
            // 1 — Stop UI recording state
            recording = false;
            updateUIWaitingToStartRecording();

            // 2 — Show error
            showErrorMessage(errorMessage2, event.getMessage());

            // 3 — If partial recording exists → ask user permission before saving
            if (event.isPartialRecordingAvailable()) {
                int option = JOptionPane.showConfirmDialog(
                        this,
                        "BITalino disconnected unexpectedly.\nA partial recording is available.\nDo you want to save it?",
                        "Unexpected Disconnection",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    startSavingProcess();
                } else {
                    // User declined saving
                    updateUIWaitingToStartRecording();
                    cardLayout.show(cardPanel, "Panel1");
                }
            }
        });
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
                    System.out.println("Starting saving process");
                    File zip = recorderService.getZipFile();
                    if (zip == null || !zip.exists()) {
                        // No ZIP available → upload cannot proceed
                        return false;
                    }

                    int patient_id= appMain.patient.getId();
                    int sampling_rate= recorderService.getFs();
                    LocalDateTime timestamp= LocalDateTime.now();
                    String filename= zip.getName();

                    byte[] zipBytes = Files.readAllBytes(zip.toPath());
                    String base64Zip = Base64.getEncoder().encodeToString(zipBytes);

                    return appMain.client.sendJsonToServer(patient_id, sampling_rate, timestamp, filename, base64Zip);
                } catch (Exception e) {
                    // If anything fails during encoding or sending
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
                        buttonsLayout.show(buttonStack, "NULL");
                        recording = false;
                    } else {
                        askRetry();
                    }
                } catch (Exception ex) {
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

    @Subscribe
    private void onClosingApp(CloseAppEvent event) {
        UIEventBus.BUS.unregister(this);
    }

}

