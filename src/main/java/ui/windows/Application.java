package ui.windows;
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

public class Application extends JFrame {

    //UI Panels
    private ArrayList<JPanel> appPanels;
    private UserLogIn logInPanel;
    private PatientMenu patientMenu;
    //#022E57
    public static Color darker_purple = new Color(114, 82, 153); //#725299
    public static Color dark_purple = new Color(170, 84, 204); //#AA54CC
    public static Color pink = new Color(226, 169, 241); //#E2A9F1
    public static Color purple = new Color(196, 158, 207);
    public static Color turquoise = new Color(94, 186, 176); //#5EBAB0
    public static Color light_purple = new Color(239, 232, 255); //#EFE8FF
    public static Color light_turquoise = new Color(193, 252, 244); //#C1FCF4
    //public static Color light_turquoise = new Color(213, 242, 236); //#d5f2ec
    public static Color lighter_turquoise = new Color(243, 250, 249);//#f3faf9
    public static Color darker_turquoise = new Color(73, 129, 122);
    public static Color dark_turquoise = new Color(52, 152, 143); //#34988f


    public static void main(String[] args) {
        Application app = new Application();
        app.showDialogIntroduceIP(app);
        //app.setVisible(true);
    }

    //Model values
    public Patient patient;
    public Doctor doctor;
    public User user;
    public Client client;
    private Integer serverPort = 9009;

    public Application() {
        appPanels = new ArrayList<JPanel>();
        initComponents();
        setBounds(100, 100, 602, 436);

        logInPanel = new UserLogIn(this);
        appPanels.add(logInPanel);
        logInPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        client = new Client( this);
        setContentPane(logInPanel);
        //changeToMainMenu();
    }

    public void initComponents() {
        setTitle("Application");
        //setSize(602, 436);
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/night_guardian_mini_500.png")).getImage());
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // handle manually

        // Window listener to stop server when closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopEverything();
            }
        });

    }

    private void hideAllPanels() {
        for (JPanel jPanel : appPanels) {
            if(jPanel.isVisible()) {
                jPanel.setVisible(false);
            }
        }
    }

    public void changeToUserLogIn() {
        hideAllPanels();
        logInPanel.setVisible(true);
        this.setContentPane(logInPanel);
    }

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

    public void changeToPanel(JPanel panel) {
        hideAllPanels();
        panel.setVisible(true);
        this.setContentPane(panel);
    }

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
        askForIP.showErrorMessage("Remember to turn off the computer firewall");
        //dialog.setSize(400, 200);

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

    private void stopEverything(){
        if(client != null && client.isConnected()){
            client.stopClient();
        }
        dispose();
    }
}
