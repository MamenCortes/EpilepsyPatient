package ui.windows;

import ui.components.MenuTemplate;
import ui.components.MyButton;
import javax.swing.*;
import java.awt.event.ActionEvent;

public class PatientMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private  ImageIcon logoIcon;
    private JButton seePatientDetails;
    private JButton seeDoctorInfo;
    private JButton seeRecordingHistory;
    private JButton seeSymptomsCalendar;
    private JButton recordBitalino;
    private JButton logOutButton;
    private JButton connectBitalino;
    private Application appMenu;
    private PatientInfo patientInfo;
    private DoctorInfo doctorInfo;
    private RecordingsHistory recordingsHistory;
    private SymptomsCalendar symptomsCalendar;
    private String company_name;

    public PatientMenu(Application appMenu) {
        //super();
        this.appMenu = appMenu;
        patientInfo = new PatientInfo(appMenu);
        doctorInfo = new DoctorInfo(appMenu);
        recordingsHistory = new RecordingsHistory(appMenu);
        symptomsCalendar = new SymptomsCalendar(appMenu);

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
        seeRecordingHistory = new MyButton("Recordings History");
        seeSymptomsCalendar = new MyButton("Symptoms History");
        connectBitalino = new MyButton("Connect Bitalino");
        recordBitalino = new MyButton("New Recording");
        logOutButton = new MyButton("Log Out");


        buttons.add(seePatientDetails);
        buttons.add(seeDoctorInfo);
        buttons.add(seeRecordingHistory);
        buttons.add(seeSymptomsCalendar);
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
        }else if(e.getSource()== seeRecordingHistory) {
            //appMenu.changeToSearchPatient();
            appMenu.changeToPanel(recordingsHistory);
        }else if(e.getSource()== recordBitalino) {
            //appMenu.changeToSearchPatient();
        }else if(e.getSource()==seeSymptomsCalendar) {
            appMenu.changeToPanel(symptomsCalendar);
        }else if(e.getSource()==logOutButton) {
            appMenu.changeToUserLogIn();
        }

    }
}
