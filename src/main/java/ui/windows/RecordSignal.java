package ui.windows;

import net.miginfocom.swing.MigLayout;
import pojos.Signal;
import ui.SignalRecorderService;
import ui.components.MyButton;
import ui.components.MyTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

//TODO: al parar de grabar que pregunte si estás seguro
//TODO: feedback sobre estableciendo la conexión y mandando datos
//TODO: si ha fallado la conexión, botón para volver a enviarlo
public class RecordSignal extends JPanel implements ActionListener {
    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", Font.ITALIC, 15);
    private final Font subHeadingFont = new Font("sansserif", 1, 20);
    private final Color contentColor = Application.dark_turquoise;
    private ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/ekg-monitor64_02.png"));
    private ImageIcon recordingGif = new ImageIcon(getClass().getResource("/icons/ecg-gif128.gif"));
    private ImageIcon recordingImg = new ImageIcon(getClass().getResource("/icons/ecg-gif128.png"));
    private ImageIcon uploadingGif = new ImageIcon(getClass().getResource("/icons/uploading128.gif"));
    private ImageIcon uploadedImg = new ImageIcon(getClass().getResource("/icons/check128.png"));

    //Components
    JLabel errorMessage;
    JLabel errorMessage2;
    MyButton okButton;
    MyButton back2MenuBt;
    MyButton stopRecording;
    MyButton startRecording;
    CardLayout cardLayout;
    CardLayout buttonsLayout;
    JPanel buttonStack;
    JPanel cardPanel;
    JLabel image;
    MyTextField iptxtField;

    //
    Boolean recording = false;
    Boolean saving = false;
    Application appMain;

    public static void main(String[] args) {
        RecordSignal symptomPanel = new RecordSignal(new Application());
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setSize(800, 600);
        frame.setBounds(100, 100, 602, 436);
        frame.setResizable(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(symptomPanel);
    }

    public RecordSignal(Application app) {
        appMain = app;
        initPanel();
    }

    private void initPanel() {
        this.setLayout(new MigLayout("fill, inset 20, wrap 4", "[25%][25%][25%][25%]", "[15%][60%][5%][5%]"));
        this.setBackground(Color.white);
        //Add Title
        JLabel title = new JLabel("Signal Monitoring");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(icon);
        add(title, "cell 0 0 4 1, alignx left");

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel recordSignalPanel = new JPanel();
        JPanel connectBitalino = new JPanel();
        connectBitalino.setBackground(Color.white);
        connectBitalino.setLayout(new MigLayout("wrap, fill, inset 20", "push[center]push", "push[]25[]10[]20[]push"));
        JLabel label = new JLabel("Introduce server MAC Address:");
        label.setFont(subHeadingFont);
        label.setForeground(contentColor);
        connectBitalino.add(label);

        iptxtField = new MyTextField();
        //iptxtField.setBackground(Application.light_purple);
        iptxtField.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/pass.png")));
        iptxtField.setHint("Enter new MAC Address...");
        connectBitalino.add(iptxtField, "w 60%");

        errorMessage = new JLabel();
        errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
        errorMessage.setFont(contentFont);
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        errorMessage.setVisible(false);

        okButton = new MyButton();
        okButton.addActionListener(this);
        okButton.setText("CONNECT");
        okButton.setBackground(Application.turquoise);
        okButton.setForeground(Color.white);

        connectBitalino.add(okButton, "center, growx");
        connectBitalino.add(errorMessage, "center, growx");

        recordSignalPanel.setLayout(new MigLayout("wrap, fill, inset 20", "push[center][center]push", "push[30%][10%][70%]push"));
        recordSignalPanel.setBackground(Color.white);
        startRecording = new MyButton("Start Recording", Application.turquoise, Color.white);
        startRecording.addActionListener(this);
        stopRecording = new MyButton("Stop Recording", Application.turquoise, Color.white);
        stopRecording.addActionListener(this);
        stopRecording.setVisible(false);
        image = new JLabel(recordingImg);

        errorMessage2 = new JLabel();
        errorMessage2.setHorizontalAlignment(SwingConstants.CENTER);
        errorMessage2.setFont(contentFont);
        errorMessage2.setForeground(Color.red);
        errorMessage2.setText("Error message test");
        errorMessage2.setVisible(false);

        buttonsLayout = new CardLayout();
        buttonStack = new JPanel(buttonsLayout);
        buttonStack.setOpaque(false);
        JLabel empty = new JLabel();
        empty.setBackground(Color.white);
        buttonStack.add(startRecording, "START");
        buttonStack.add(stopRecording, "STOP");
        buttonStack.add(empty, "NULL");
        buttonsLayout.show(buttonStack, "START");

        recordSignalPanel.add(buttonStack, "cell 0 2, center, growx");

        recordSignalPanel.add(image, "cell 0 0 2 1, growx");
        recordSignalPanel.add(errorMessage2, "cell 0 1 2 1, growx");
        //recordSignalPanel.add(startRecording, "cell 0 2 2 1, center, growx");
        //recordSignalPanel.add(stopRecording, "cell 0 2 2 1, center, growx");

        back2MenuBt = new MyButton();
        back2MenuBt.addActionListener(this);
        back2MenuBt.setText("BACK TO MENU");
        //cancelButton.setBackground(Application.turquoise);
        //cancelButton.setForeground(Color.white);

        cardPanel.add(connectBitalino, "Panel1");
        cardPanel.add(recordSignalPanel, "Panel2");

        // Mostrar un panel:
        cardLayout.show(cardPanel, "Panel1");
        add(cardPanel, "cell 0 1 4 1, grow");
        //add(errorMessage,"cell 0 2 4 1, growx, center" );
        add(back2MenuBt, "cell 0 2 4 1, center");
    }

    private void showErrorMessage(JLabel errorMessage, String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setForeground(Color.red);
    }

    private void showFeedbackMessage(JLabel errorMessage, String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setForeground(contentColor);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okButton){
            showFeedbackMessage(errorMessage, "Connecting to Bitalino...");
            showFeedbackMessage(errorMessage2, "Clic start to start recording");
            String macAdd = iptxtField.getText();
            System.out.println(macAdd);

            //TODO: Call functions to Connect to bitalino and manage errors
            SignalRecorderService recorderService = new SignalRecorderService(macAdd);
            recorderService.startRecording();
            if (recorderService.isRecording()) {
                cardLayout.show(cardPanel, "Panel2");
            }

        }else if(e.getSource() == back2MenuBt){
            cardLayout.show(cardPanel, "Panel1");
            resetPanel();
            appMain.changeToMainMenu();
        } else if (e.getSource() == startRecording) {
            if(!recording){
                image.setIcon(recordingGif);
                showFeedbackMessage(errorMessage2, "Recording...");
                recording = true;
                back2MenuBt.setVisible(false);
                buttonsLayout.show(buttonStack, "STOP");
            }
        } else if (e.getSource() == stopRecording) {
            if(recording){
                int option = JOptionPane.showConfirmDialog(this,"Are you sure you want to stop the recording?");
                if(option == JOptionPane.YES_OPTION){
                    buttonsLayout.show(buttonStack, "NULL");
                    image.setIcon(uploadingGif);
                    showFeedbackMessage(errorMessage2, "Saving recording...");
                    // 2) Lanzar proceso en background
                    startSavingProcess();
                }
            }

        }
    }

    private void askRetry() {
        int option = JOptionPane.showConfirmDialog(
                null,
                "Error saving the signal.\n¿Do you want to retry?",
                "Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            startSavingProcess();  //vuelve a ejecutar el proceso
        }else if (option == JOptionPane.NO_OPTION) {
            image.setIcon(null);
            showErrorMessage(errorMessage2, "Error saving signal");
            back2MenuBt.setVisible(true);
            buttonsLayout.show(buttonStack, "START");
            recording = false;
        }
    }

    private void startSavingProcess() {
        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {
                try {
                    //TODO: call functions save signals

                    Thread.sleep(3000);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        image.setIcon(uploadedImg);
                        showFeedbackMessage(errorMessage2, "Signal saved successfully.");
                        back2MenuBt.setVisible(true);
                        //startRecording.setVisible(true);
                        buttonsLayout.show(buttonStack, "START");
                        recording = false;
                    } else {
                        askRetry();
                    }

                } catch (Exception e) {
                    askRetry();
                }
            }
        }.execute();
    }

    private void resetPanel() {
        cardLayout.show(cardPanel, "Panel1");
        buttonsLayout.show(buttonStack, "START");
        image.setIcon(recordingImg);
        errorMessage.setVisible(false);
        errorMessage.setVisible(false);
        iptxtField.setText("");
    }

}

