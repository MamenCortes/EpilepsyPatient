package ui.windows;

import pojos.Doctor;
import pojos.ModelManager;
import pojos.Report;
import ui.components.MenuTemplate;
import ui.components.MyButton;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;

public class PatientMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private  ImageIcon logoIcon;
    private JButton seePatientDetails;
    private JButton seeDoctorInfo;
    private JButton seeRecordingHistory;
    private JButton seeSymptomsCalendar;
    private JButton recordBitalino;
    private JButton logOutButton;
    private JButton newSymptom;
    private Application appMenu;
    private PatientInfo patientInfo;
    private DoctorInfo doctorInfo;
    private RecordingsHistory recordingsHistory;
    private SymptomsCalendar symptomsCalendar;
    private NewSymptomPanel newSymptomPanel;
    private String company_name;

    public PatientMenu(Application appMenu) {
        //super();
        this.appMenu = appMenu;
        patientInfo = new PatientInfo(appMenu);
        doctorInfo = new DoctorInfo(appMenu);
        recordingsHistory = new RecordingsHistory(appMenu);
        symptomsCalendar = new SymptomsCalendar(appMenu);
        newSymptomPanel = new NewSymptomPanel(appMenu);

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
        newSymptom = new MyButton("New Symptoms");
        recordBitalino = new MyButton("New Recording");
        logOutButton = new MyButton("Log Out");


        buttons.add(seePatientDetails);
        buttons.add(seeDoctorInfo);
        buttons.add(seeRecordingHistory);
        buttons.add(seeSymptomsCalendar);
        buttons.add(newSymptom);
        buttons.add(recordBitalino);
        buttons.add(logOutButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seeDoctorInfo) {
            //appMenu.changeToAddPatient();
            Doctor doctor = null;
            if(appMenu.doctor == null) {
                try {
                    doctor = appMenu.client.getDoctorFromPatient(appMenu.patient.getDoctor_id(), appMenu.patient.getId(), appMenu.user.getId());appMenu.changeToPanel(doctorInfo);
                    System.out.println("Doctor = "+doctor);
                    appMenu.doctor = doctor;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            System.out.println("Doctor ="+ doctor);
            doctorInfo.updateDoctorForm(doctor);
            appMenu.changeToPanel(doctorInfo);
        }else if(e.getSource()== seePatientDetails) {
            //appMenu.changeToSearchPatient();
            patientInfo.updatePatientForm(appMenu.patient);
            appMenu.changeToPanel(patientInfo);
        }else if(e.getSource()== seeRecordingHistory) {
            //appMenu.changeToSearchPatient();
            recordingsHistory.updateSignalRecordingsList(ModelManager.generateRandomSignalRecordings());
            appMenu.changeToPanel(recordingsHistory);
        }else if(e.getSource()== recordBitalino) {
            //appMenu.changeToSearchPatient();
        }else if(e.getSource()==seeSymptomsCalendar) {
            symptomsCalendar.updateData(appMenu.patient.getSymptomsList());
            appMenu.changeToPanel(symptomsCalendar);
        }else if(e.getSource()==logOutButton) {
            appMenu.doctor = null;
            appMenu.patient = null;
            appMenu.user = null;
            appMenu.changeToUserLogIn();
        } else if (e.getSource()==newSymptom) {
            appMenu.changeToPanel(newSymptomPanel);
        }

    }
}
