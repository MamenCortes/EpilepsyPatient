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
        app.setVisible(true);
    }

    public Application() {
        appPanels = new ArrayList<JPanel>();
        initComponents();
        setBounds(100, 100, 602, 436);

        logInPanel = new UserLogIn(this);
        appPanels.add(logInPanel);
        logInPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        //setContentPane(logInPanel);
        changeToPatientMenu();
    }

    public void initComponents() {
        setTitle("Application");
        //setSize(602, 436);
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/night_guardian_mini_500.png")).getImage());
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

    public void changeToPanel(JPanel panel) {
        hideAllPanels();
        panel.setVisible(true);
        this.setContentPane(panel);
    }
}
