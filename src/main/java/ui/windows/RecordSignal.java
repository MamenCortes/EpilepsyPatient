package ui.windows;

import net.miginfocom.swing.MigLayout;
import ui.components.MyButton;
import ui.components.MyTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecordSignal extends JPanel implements ActionListener {
    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", Font.ITALIC, 15);
    private final Font subHeadingFont = new Font("sansserif", 1, 20);
    private final Color contentColor = Application.dark_turquoise;
    private ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/ekg-monitor64_02.png"));
    private ImageIcon gif  = new ImageIcon(getClass().getResource("/icons/ecg-gif128.gif"));
    private ImageIcon img  = new ImageIcon(getClass().getResource("/icons/ecg-gif128.png"));

    //Components
    JLabel errorMessage;
    JLabel errorMessage2;
    MyButton okButton;
    MyButton back2MenuBt;
    MyButton stopRecording;
    MyButton startRecording;
    CardLayout cardLayout;
    JPanel cardPanel;
    JLabel image;

    //
    Boolean recording = false;

    public static void main(String[] args) {
        RecordSignal symptomPanel = new RecordSignal();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setSize(800, 600);
        frame.setBounds(100, 100, 602, 436);
        frame.setResizable(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(symptomPanel);
    }

    public RecordSignal() {
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

        //TODO: recording signal
        JPanel recordSignalPanel = new JPanel();
        JPanel connectBitalino = new JPanel();
        connectBitalino.setBackground(Color.white);
        connectBitalino.setLayout(new MigLayout("wrap, fill, inset 20", "push[center]push", "push[]25[]10[]20[]push"));
        JLabel label = new JLabel("Introduce server MAC Address:");
        label.setFont(subHeadingFont);
        label.setForeground(contentColor);
        connectBitalino.add(label);

        MyTextField iptxtField = new MyTextField();
        //iptxtField.setBackground(Application.light_purple);
        iptxtField.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/pass.png")));
        iptxtField.setHint("Enter new MAC Address...");
        connectBitalino.add(iptxtField, "w 60%");

        errorMessage = new JLabel();
        errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
        errorMessage.setFont(contentFont);
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        errorMessage.setVisible(true);

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
        image = new JLabel(img);

        errorMessage2 = new JLabel();
        errorMessage2.setHorizontalAlignment(SwingConstants.CENTER);
        errorMessage2.setFont(contentFont);
        errorMessage2.setForeground(Color.red);
        errorMessage2.setText("Error message test");
        errorMessage2.setVisible(true);

        recordSignalPanel.add(image, "cell 0 0 2 1, growx");
        recordSignalPanel.add(errorMessage2, "cell 0 1 2 1, growx");
        recordSignalPanel.add(startRecording, "center, growx");
        recordSignalPanel.add(stopRecording, "center, growx");

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
            //TODO: Connect to bitalino
            cardLayout.show(cardPanel, "Panel2");
        }else if(e.getSource() == back2MenuBt){
            cardLayout.show(cardPanel, "Panel1");
        } else if (e.getSource() == startRecording) {
            image.setIcon(gif);
            showFeedbackMessage(errorMessage2, "Recording...");
            if(recording != true){
                recording = true;
                back2MenuBt.setVisible(false);
            }
        } else if (e.getSource() == stopRecording) {
            image.setIcon(img);
            showFeedbackMessage(errorMessage2, "Recording stopped");
            if(recording == true){
                back2MenuBt.setVisible(true);
                recording = false;
            }
        }
    }
}

