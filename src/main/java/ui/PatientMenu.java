package ui;

import ui.components.MenuTemplate;
import ui.components.MyButton;

import javax.swing.JButton;
import java.awt.event.ActionEvent;

public class PatientMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private JButton addPatientButton;
    private JButton searchPatientButton;
    private JButton logOutButton;
    private Application appMenu;

    public PatientMenu(Application appMenu) {
        super();
        this.appMenu = appMenu;
        addButtons();
        this.init();
    }

    private void addButtons() {
        addPatientButton = new MyButton("Add Patient");
        searchPatientButton = new MyButton("Search Patient");
        logOutButton = new MyButton("Log Out");

        buttons.add(addPatientButton);
        buttons.add(searchPatientButton);
        buttons.add(logOutButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==addPatientButton) {
            //appMenu.changeToAddPatient();

        }else if(e.getSource()==searchPatientButton) {
            //appMenu.changeToSearchPatient();
        }else if(e.getSource()==logOutButton) {
            appMenu.changeToUserLogIn();
        }

    }
}
