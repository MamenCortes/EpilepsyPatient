package ui;

import ui.components.MenuTemplate;
import ui.components.MyButton;
import javax.swing.*;
import java.awt.event.ActionEvent;

public class PatientMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private  ImageIcon logoIcon;
    private JButton seePatientDetails;
    private JButton seeDoctorInfo;
    private JButton seeReportHistory;
    private JButton recordBitalino;
    private JButton logOutButton;
    private JButton connectBitalino;
    private Application appMenu;
    private PatientInfo patientInfo;
    private DoctorInfo doctorInfo;
    private ReportsHistory reportsHistory;
    private String company_name;

    public PatientMenu(Application appMenu) {
        //super();
        this.appMenu = appMenu;
        patientInfo = new PatientInfo(appMenu);
        doctorInfo = new DoctorInfo(appMenu);
        reportsHistory = new ReportsHistory(appMenu);
        addButtons();
        company_name = "NIGHT GUARDIAN: EPILEPSY";
        //company_name = "<html>NIGHT GUARDIAN<br>EPILEPSY</html>";
        //company_name ="<html><div style='text-align: center;'>NIGHT GUARDIAN<br>EPILEPSY</div></html>";

        logoIcon = new ImageIcon(getClass().getResource("/icons/night_guardian_mini_128.png"));
        this.init(logoIcon, company_name);
    }

    private void addButtons() {
        //Default color: light purple
        seePatientDetails = new MyButton("See My Details");
        seeDoctorInfo = new MyButton("My Physician");
        seeReportHistory = new MyButton("Report History");
        connectBitalino = new MyButton("Connect Bitalino");
        recordBitalino = new MyButton("New Recording");
        logOutButton = new MyButton("Log Out");


        buttons.add(seePatientDetails);
        buttons.add(seeDoctorInfo);
        buttons.add(seeReportHistory);
        buttons.add(connectBitalino);
        buttons.add(recordBitalino);
        buttons.add(logOutButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seeDoctorInfo) {
            //appMenu.changeToAddPatient();
            appMenu.changeToPanel(doctorInfo);
        }else if(e.getSource()== seePatientDetails) {
            //appMenu.changeToSearchPatient();
            appMenu.changeToPanel(patientInfo);
        }else if(e.getSource()== seeReportHistory) {
            //appMenu.changeToSearchPatient();
            appMenu.changeToPanel(reportsHistory);
        }else if(e.getSource()== recordBitalino) {
            //appMenu.changeToSearchPatient();
        }else if(e.getSource()==logOutButton) {
            appMenu.changeToUserLogIn();
        }

    }
}
