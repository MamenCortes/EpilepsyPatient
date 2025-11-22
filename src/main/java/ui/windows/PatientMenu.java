package ui.windows;

import network.Client;
import network.SendSignalMetadataToServer;
import network.SendZipToServer;
import pojos.Doctor;
import pojos.ModelManager;
import pojos.Signal;
import ui.SignalRecorderService;
import ui.components.MenuTemplate;
import ui.components.MyButton;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

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
    private PatientInfo patientInfoPanel;
    private DoctorInfo doctorInfoPanel;
    private RecordingsHistory recordingsHistoryPanel;
    private SymptomsCalendar symptomsCalendarPanel;
    private NewSymptomPanel newSymptomPanel;
    private RecordSignal recordSignalPanel;
    private String company_name;

    public PatientMenu(Application appMenu) {
        //super();
        this.appMenu = appMenu;
        patientInfoPanel = new PatientInfo(appMenu);
        doctorInfoPanel = new DoctorInfo(appMenu);
        recordingsHistoryPanel = new RecordingsHistory(appMenu);
        symptomsCalendarPanel = new SymptomsCalendar(appMenu);
        newSymptomPanel = new NewSymptomPanel(appMenu);
        recordSignalPanel = new RecordSignal(appMenu);

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
            if(appMenu.doctor == null) {
                Doctor doctor = null;
                try {
                    appMenu.doctor = appMenu.client.getDoctorFromPatient(appMenu.patient.getDoctor_id(), appMenu.patient.getId(), appMenu.user.getId());
                    System.out.println("Doctor received from server ="+ appMenu.doctor);
                } catch (IOException | InterruptedException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            //System.out.println("Doctor ="+ appMenu.doctor);
            doctorInfoPanel.updateDoctorForm(appMenu.doctor);
            appMenu.changeToPanel(doctorInfoPanel);
        }else if(e.getSource()== seePatientDetails) {
            //appMenu.changeToSearchPatient();
            patientInfoPanel.updatePatientForm(appMenu.patient);
            appMenu.changeToPanel(patientInfoPanel);
        }else if(e.getSource()== seeRecordingHistory) {
            //appMenu.changeToSearchPatient();
            recordingsHistoryPanel.updateSignalRecordingsList(ModelManager.generateRandomSignalRecordings());
            appMenu.changeToPanel(recordingsHistoryPanel);
        }else if(e.getSource()== recordBitalino) {
            appMenu.changeToPanel(recordSignalPanel);
        }else if(e.getSource()==seeSymptomsCalendar) {
            symptomsCalendarPanel.updateData(appMenu.patient.getSymptomsList());
            appMenu.changeToPanel(symptomsCalendarPanel);
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
