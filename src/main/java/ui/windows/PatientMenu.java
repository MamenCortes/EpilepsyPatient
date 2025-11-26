package ui.windows;

import pojos.Doctor;
import pojos.ModelManager;
import ui.components.MenuTemplate;
import ui.components.MyButton;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
/**
 * Main navigation menu for the patient-facing application.
 * <p>
 * This panel acts as the central hub from which the patient can access:
 * <ul>
 *     <li>Personal information</li>
 *     <li>Physician details</li>
 *     <li>Recording history</li>
 *     <li>Symptoms calendar</li>
 *     <li>New symptom reporting</li>
 *     <li>Signal recording through BITalino</li>
 * </ul>
 * All navigation actions are handled through {@link #actionPerformed(ActionEvent)}.
 * </p>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>{@code PatientMenu} is created once inside {@link Application#changeToMainMenu()}.</li>
 *     <li>All subpanels (patient info, doctor info, symptom calendar...) are also created once here to be reused.</li>
 *     <li>Each time this menu is shown again, none of the panels are reinitialized — instead,
 *         they are updated by their own update methods (e.g., {@code updatePatientForm()},
 *         {@code updateDoctorForm()}, {@code updateSignalRecordingsList()}).</li>
 *     <li>After navigating to a child panel, the application replaces the current content pane.</li>
 *     <li>On logout, the application clears global data (patient, doctor, user) and returns to login.</li>
 * </ul>
 *
 * @author MamenCortes
 */
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

    /**
     * Constructs the patient main menu and initializes all reusable subpanels.
     * The menu buttons are created and registered for user interaction.
     *
     * @param appMenu the main application controller, used for navigation
     *                and access to current session data.
     */
    public PatientMenu(Application appMenu) {
        this.appMenu = appMenu;
        patientInfoPanel = new PatientInfo(appMenu);
        doctorInfoPanel = new DoctorInfo(appMenu);
        recordingsHistoryPanel = new RecordingsHistory(appMenu);
        symptomsCalendarPanel = new SymptomsCalendar(appMenu);
        newSymptomPanel = new NewSymptomPanel(appMenu);
        recordSignalPanel = new RecordSignal(appMenu);

        addButtons();
        company_name = "NIGHT GUARDIAN: EPILEPSY";
        logoIcon = new ImageIcon(getClass().getResource("/icons/night_guardian_mini_128.png"));
        this.init(logoIcon, company_name);
    }

    /**
     * Creates and adds all menu navigation buttons to the inherited component list.
     * This method is called only once during construction.
     */
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

    /**
     * Handles all menu button actions and switches the application to the corresponding panel.
     * <ul>
     *     <li><b>My Physician</b> — Requests the doctor from server if not already loaded,
     *         updates {@code doctorInfoPanel} and opens it.</li>
     *     <li><b>See My Details</b> — Updates patient info and opens the {@code PatientInfo} panel.</li>
     *     <li><b>Recordings History</b> — Loads or refreshes the list of recordings.</li>
     *     <li><b>Symptoms History</b> — Updates the symptom calendar with current reports.</li>
     *     <li><b>New Symptoms</b> — Opens a panel that lets the patient submit a new report.</li>
     *     <li><b>New Recording</b> — Opens BITalino signal recording workflow.</li>
     *     <li><b>Log Out</b> — Clears global user, patient and doctor data and returns to login.</li>
     * </ul>
     *
     * @param e the button click event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seeDoctorInfo) {
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
            doctorInfoPanel.updateDoctorForm(appMenu.doctor);
            appMenu.changeToPanel(doctorInfoPanel);
        }else if(e.getSource()== seePatientDetails) {
            patientInfoPanel.updatePatientForm(appMenu.patient);
            appMenu.changeToPanel(patientInfoPanel);
        }else if(e.getSource()== seeRecordingHistory) {
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
