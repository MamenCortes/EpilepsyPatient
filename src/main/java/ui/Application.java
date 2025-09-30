package ui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class Application extends JFrame {

    //UI Panels
    private ArrayList<JPanel> appPanels;
    private UserLogIn logInPanel;
    private PatientMenu patientMenu;

    public static void main(String[] args) {
        Application app = new Application();
        app.setVisible(true);
    }

    public Application() {
        appPanels = new ArrayList<JPanel>();
        initComponents();
        setBounds(100, 100, 602, 436);

        logInPanel = new UserLogIn(this);
        appPanels.add(logInPanel);
        logInPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(logInPanel);
    }

    public void initComponents() {
        setTitle("Application");
        //setSize(602, 436);
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/epilepsy512.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

    public void changeToPatientMenu(){
        hideAllPanels();
        if (patientMenu == null) {
            patientMenu = new PatientMenu(this);
            appPanels.add(patientMenu);
            System.out.println("Patient Panel initialized");
        }

        patientMenu.setVisible(true);
        this.setContentPane(patientMenu);

    }
}
