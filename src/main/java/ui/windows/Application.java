package ui.windows;
import BITalino.DetectionManager;
import Events.*;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import pojos.Doctor;
import pojos.Patient;
import pojos.User;
import network.Client;
import ui.components.AskQuestionDialog;
import ui.components.MyButton;
import ui.components.MyTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main application window and controller responsible for:
 * <ul>
 *     <li>Initializing UI panels and coordinating navigation</li>
 *     <li>Managing the connection to the remote server</li>
 *     <li>Handling login state for {@link User} and {@link Patient}</li>
 *     <li>Generating shared resources (e.g., symptom colors)</li>
 * </ul>
 *
 * <h3>Panel Management</h3>
 * Panels such as {@link UserLogIn}, {@link PatientMenu}, and other dynamic views
 * are created and stored in an internal list. The controller provides helper
 * methods for transitions:
 * <ul>
 *     <li>{@link #changeToUserLogIn()}</li>
 *     <li>{@link #changeToMainMenu()}</li>
 *     <li>{@link #changeToPanel(JPanel)}</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>The application window is created in {@link #main(String[])}.</li>
 *     <li>A server IP dialog is shown before the app becomes visible.</li>
 *     <li>The user must authenticate before accessing other panels.</li>
 *     <li>If the server disconnects, a new dialog prompts for reconnection.</li>
 * </ul>
 *
 * @author MamenCortes
 */
public class Application extends JFrame {

    //UI Panels
    private ArrayList<JPanel> appPanels;
    private UserLogIn logInPanel;
    private PatientMenu patientMenu;

    //Color palette of the App
    public static Color darker_purple = new Color(114, 82, 153); //#725299
    public static Color dark_purple = new Color(170, 84, 204); //#AA54CC
    public static Color pink = new Color(226, 169, 241); //#E2A9F1
    public static Color purple = new Color(196, 158, 207);
    public static Color turquoise = new Color(94, 186, 176); //#5EBAB0
    public static Color light_purple = new Color(239, 232, 255); //#EFE8FF
    public static Color light_turquoise = new Color(193, 252, 244); //#C1FCF4
    public static Color lighter_turquoise = new Color(243, 250, 249);//#f3faf9
    public static Color darker_turquoise = new Color(73, 129, 122);
    public static Color dark_turquoise = new Color(52, 152, 143); //#34988f
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Future<?> pendingAlarm = null;
    /**
     * Entry point of the graphical application.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        Application app = new Application();
        app.showDialogIntroduceIP(app);
    }

    //Model values
    public Patient patient;
    public Doctor doctor;
    public User user;
    public Client client;
    private Integer serverPort = 9009;
    public DetectionManager detectionManager;

    /**
     * Constructs the application window:
     * <ul>
     *     <li>Initializes frame and default window settings</li>
     *     <li>Creates the login panel</li>
     *     <li>Initializes network client</li>
     *     <li>Prepares global color mappings for symptoms</li>
     * </ul>
     */
    public Application() {
        appPanels = new ArrayList<JPanel>();
        initComponents();
        setBounds(100, 100, 602, 436);

        //Initialize logIn panel
        logInPanel = new UserLogIn(this);
        appPanels.add(logInPanel);
        logInPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(logInPanel);

        //Initialize client
        client = new Client();

        //Subscribe to events
        detectionManager = new DetectionManager();
        UIEventBus.BUS.register(this); //Listens for events
    }

    @Subscribe
    public void onShowHelpDialog(ShowHelpDialogEvent event){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int result = JOptionPane.showConfirmDialog(
                        null,
                        "We detected abnormal heart rate and movement.\nDo you need help?",
                        "Possible Seizure Detected",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                startOneMinuteTimer(() -> onTimeoutNoResponse());
                if (result == JOptionPane.NO_OPTION) {
                    // User is fine – inform DetectionManager
                    detectionManager.onUserOk();
                    if (pendingAlarm != null && !pendingAlarm.isDone()) {
                        pendingAlarm.cancel(true);
                    }

                    System.out.println("🟢 Patient responded on time");

                }
            }
        });
    }
    private void onTimeoutNoResponse() {
        System.out.println("🔴 patient did not respond → ALERT");

        SwingUtilities.invokeLater(() -> {
            // Aquí va lo que quieres que pase si NO responde
            String alert=client.sendAlertToAdmin();
            showMessageDialog(this, alert);
        });
    }
    @Subscribe
    public void onAllertConfirmed(AlertMessageEvent event) {
        String alert=client.sendAlertToAdmin();
        showMessageDialog(this, alert);
    }
    /**
     * Called automatically when the connection to the server is lost.
     * Shows an error message and prompts the user to enter a new IP.
     */
    @Subscribe
    public void onServerDisconnected(ServerDisconnectedEvent event) {
        SwingUtilities.invokeLater(() -> {

            /*JOptionPane.showMessageDialog(
                    null,
                    "The conexion with the server was interrupted.",
                    "Conexion error",
                    JOptionPane.ERROR_MESSAGE
            );*/

            showMessageDialog(this, "The conexion with the server was interrupted");

            // Espera a que el message dialog finalice y luego lanza el siguiente diálogo
            SwingUtilities.invokeLater(() -> {
                this.showDialogIntroduceIP(this);
                System.out.println("Requested new IP address");
            });
        });
    }

    /**
     * Utility method to display a custom message dialog using a Night Guardian–styled window.
     *
     * @param parentFrame the parent frame for centering the dialog
     * @param message the message to display
     */
    public static void showMessageDialog(JFrame parentFrame, String message) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap, fill, inset 15", "[center]", "push[]25[]push"));
        panel.setBackground(Color.white);
        panel.setPreferredSize(new Dimension(250, 150));

        JLabel label = new JLabel(message);
        label.setFont(new Font("sansserif", 1, 25));
        label.setForeground(Application.dark_purple);

        JTextArea labelLikeText = new JTextArea(message);
        labelLikeText.setLineWrap(true);
        labelLikeText.setWrapStyleWord(true);
        labelLikeText.setEditable(false);
        labelLikeText.setOpaque(false); // looks like a JLabel
        labelLikeText.setFont(new Font("sansserif", 1, 20));
        labelLikeText.setBackground(Color.white);
        labelLikeText.setForeground(Application.dark_purple);
        panel.add(labelLikeText, "growx, center, wrap");

        MyButton okButton = new MyButton("OK", Application.turquoise, Color.white);
        panel.add(okButton, "center");

        JDialog dialog = new JDialog(parentFrame, "Message dialog", true); //dont allow interacting with other panels at the same time
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {dialog.dispose();}
        });

        dialog.setVisible(true);
    }



    /**
     * Initializes frame title, icon, default close behavior, and window listeners.
     */
    public void initComponents() {
        setTitle("Patient Application");
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/night_guardian_mini_500.png")).getImage());

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // handle manually

        // Window listener to stop server when closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopEverything();
            }
        });

    }
    /**
     * Hides all currently visible panels in preparation for a panel transition.
     */
    private void hideAllPanels() {
        for (JPanel jPanel : appPanels) {
            if(jPanel.isVisible()) {
                jPanel.setVisible(false);
            }
        }
    }
    /**
     * Switches to the login panel after hiding all other panels.
     * Used after logging out.
     */
    public void changeToUserLogIn() {
        hideAllPanels();
        logInPanel.setVisible(true);
        this.setContentPane(logInPanel);
    }
    /**
     * Switches to the main menu panel. If it has never been created,
     * it is instantiated here.
     */
    public void changeToMainMenu(){
        hideAllPanels();
        if (patientMenu == null) {
            patientMenu = new PatientMenu(this);
            appPanels.add(patientMenu);
            System.out.println("Patient Panel initialized");
        }

        patientMenu.setVisible(true);
        this.setContentPane(patientMenu);

    }
    /**
     * Displays the provided panel. If not already present in the internal list,
     * it is added.
     *
     * @param panel the panel to display
     */
    public void changeToPanel(JPanel panel) {
        hideAllPanels();
        panel.setVisible(true);
        this.setContentPane(panel);
    }
    /**
     * Displays a modal dialog requesting the server's IP address.
     * <p>
     * If the connection succeeds, the application becomes visible.
     * If it fails, the user is asked again or can exit the application.
     * </p>
     *
     * @param parentFrame the frame to anchor the dialog
     */
    private void showDialogIntroduceIP(JFrame parentFrame) {
        MyTextField ipTextField = new MyTextField();
        MyButton okButton = new MyButton("OK");
        MyButton cancelButton = new MyButton("Cancel");

        AskQuestionDialog askForIP = new AskQuestionDialog(ipTextField, okButton, cancelButton);
        askForIP.setBackground(Color.white);
        askForIP.setPreferredSize(new Dimension(400, 300));

        JDialog dialog = new JDialog(parentFrame, "Server IP", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.getContentPane().add(askForIP);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        askForIP.showErrorMessage("Remember to turn off the computer firewall");;

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipTextField.getText();
                if(ip != null && !ip.isBlank()) {
                    try {
                        if (client.connect(ip, serverPort)) {
                            parentFrame.setVisible(true);
                            dialog.dispose();
                        } else {
                            askForIP.showErrorMessage("Server IP Error");
                        }
                    }catch (Exception ex) {
                        askForIP.showErrorMessage("Could not connect to server");
                    }
                }else{
                    askForIP.showErrorMessage("Please enter an IP Address");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                stopEverything();
            }
        });

        dialog.setVisible(true);
    }
    /**
     * Stops the network client if connected, disposes of the window,
     * and terminates the program.
     */
    public void stopEverything(){
        if(client != null && client.isConnected()){
            client.stopClient(true);
        }
        dispose();
        //unsuscribe from events
        UIEventBus.BUS.unregister(this);
        System.exit(0);
    }
    private void startOneMinuteTimer(Runnable onTimeout) {

        // Cancelar temporizador anterior si existe
        if (pendingAlarm != null && !pendingAlarm.isDone()) {
            pendingAlarm.cancel(true);
        }

        // Programar el timeout de 60s
        pendingAlarm = scheduler.schedule(() -> {
            onTimeout.run();   // Esto se ejecuta si NO hay respuesta
        }, 60, TimeUnit.SECONDS);
    }

}
