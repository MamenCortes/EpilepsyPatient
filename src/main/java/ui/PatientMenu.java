package ui;

import ui.components.MenuTemplate;
import ui.components.MyButton;

import javax.swing.JButton;
import java.awt.event.ActionEvent;

public class PatientMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private JButton seePatientDetails;
    private JButton seeDoctorInfo;
    private JButton seeReportHistory;
    private JButton recordBitalino;
    private JButton logOutButton;
    private Application appMenu;
    private PatientInfo patientInfo;

    public PatientMenu(Application appMenu) {
        //super();
        this.appMenu = appMenu;
        patientInfo = new PatientInfo(appMenu);
        addButtons();
        this.init();
    }

    private void addButtons() {
        seePatientDetails = new MyButton("See My Details");
        seeDoctorInfo = new MyButton("My Physician");
        seeReportHistory = new MyButton("Report History");
        recordBitalino = new MyButton("New Recording");
        logOutButton = new MyButton("Log Out");

        buttons.add(seePatientDetails);
        buttons.add(seeDoctorInfo);
        buttons.add(seeReportHistory);
        buttons.add(recordBitalino);
        buttons.add(logOutButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seeDoctorInfo) {
            //appMenu.changeToAddPatient();
        }else if(e.getSource()== seePatientDetails) {
            //appMenu.changeToSearchPatient();
            appMenu.changeToPanel(patientInfo);
        }else if(e.getSource()== seeReportHistory) {
            //appMenu.changeToSearchPatient();
        }else if(e.getSource()== recordBitalino) {
            //appMenu.changeToSearchPatient();
        }else if(e.getSource()==logOutButton) {
            appMenu.changeToUserLogIn();
        }

    }
}
