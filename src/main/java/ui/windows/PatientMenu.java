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
    private final SignalRecorderService recorderService = new SignalRecorderService();

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
            Doctor doctor = null;
            if(appMenu.doctor == null) {
                try {
                    doctor = appMenu.client.getDoctorFromPatient(appMenu.patient.getDoctor_id(), appMenu.patient.getId(), appMenu.user.getId());appMenu.changeToPanel(doctorInfoPanel);
                    System.out.println("Doctor = "+doctor);
                    appMenu.doctor = doctor;
                } catch (IOException | InterruptedException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            System.out.println("Doctor ="+ doctor);
            doctorInfoPanel.updateDoctorForm(doctor);
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
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Start a new BITalino recording?",
                    "New Recording",
                    JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                // Lanzamos la grabación en un hilo para no bloquear la UI
                new Thread(() -> {
                    try {
                        recorderService.startRecording();
                       // TODOañadir panel de progreso o similar
                        // TODO cuando el usuario cierre el diálogo, paramos
                        recorderService.stopRecording();
                        if (recorderService.isRecordingInterrupted()) {
                            // TODO le dice al usuario q la señal del bitalino se ha interrumpido y que se mandara lo que si se haya grabado
                        }
                        // construimos la Signal asociada al paciente
                        int patientId = appMenu.patient.getId(); // o como lo tengas
                        Signal signal = recorderService.buildSignalForPatient();

                        String json = signal.buildSignalMetadataJson(signal,patientId);
                        System.out.println("JSON metadata:\n" + json);
                        //TODO pedir al cliente el ip
                        int port1 = 9000;
                        int port2 = 9009;
                        String ip = "localhost";
                        SendZipToServer zipClient = new SendZipToServer(ip, port1);
                        boolean zipOk = zipClient.sendZipToServer(signal.getRawSignal());
                        // TODO manejar la respuesta del servidor con boolean zipOk y si false remandar el zip
                        SendSignalMetadataToServer metaClient = new SendSignalMetadataToServer(ip, port2);
                        boolean metaOk = metaClient.sendMetadataJson(json);

                        /*SwingUtilities.invokeLater(() -> {
                            if (zipOk && metaOk) {
                                JOptionPane.showMessageDialog(this,
                                        "Recording saved and sent to your doctor.");
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "Recording finished, but there was an error sending data.",
                                        "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        });*/
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this,
                                        "Error recording BITalino",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE)
                        );
                    }
                }).start();
            }
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
