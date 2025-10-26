package ui.windows;

import model.Patient;
import net.miginfocom.swing.MigLayout;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class PatientInfo extends JPanel implements ActionListener {
    private Application appMain;
    private JLabel nameHeading;
    private MyTextField name;
    private JLabel surnameHeading;
    private MyTextField surname;
    private JLabel emailHeading;
    private MyTextField email;
    private JLabel phoneHeading;
    private MyTextField phoneNumber;
    private JLabel sexHeading;
    private MyTextField sex;
    private MyComboBox<String> nextStep;
    private JLabel birthDateHeading;
    private MyTextField birthDate;

    private JLabel title;
    protected String titleText = " ";
    protected JButton backButton;
    protected JButton nextButton;
    protected JButton deleteButton;
    protected JButton applyChanges;
    protected JButton goBackButton;
    protected JLabel errorMessage;
    protected JPanel formContainer;


    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;
    private Color textFieldBg = new Color(230, 245, 241);

    private Patient patient;

    //private JDateChooser birthDate;
    public PatientInfo(Application appMain) {
        this.appMain = appMain;
        patient = Application.patient;
        initPatientInfo();

    }

    public void initPatientInfo() {
        this.titleText = "Patient information";

        //Initialize values
        //TODO: replace with actual patient values
        name = new MyTextField();
        //name.setText("Jane");
        name.setText(patient.getName());
        name.setEnabled(false); //Doesnt allow editing
        surname = new MyTextField();
        //surname.setText("Doe");
        surname.setText(patient.getSurname());
        surname.setEnabled(false);
        email = new MyTextField();
        //email.setText("jane.doe@gmail.com");
        email.setText(patient.getEmail());
        email.setEnabled(false);
        phoneNumber = new MyTextField();
        //phoneNumber.setText("123456789");
        phoneNumber.setText(Integer.toString(patient.getPhone()));
        phoneNumber.setEnabled(false);
        sex = new MyTextField();
        //sex.setText("Non Binary");
        sex.setText(patient.getSex());
        sex.setEnabled(false);
        birthDate = new MyTextField();
        //birthDate.setText("1999-11-11");
        birthDate.setText(patient.getDateOfBirth());
        birthDate.setEnabled(false);
        formContainer = new JPanel();
        initPatientForm();
    }

    private void initPatientForm() {
        //this.setLayout(new MigLayout("fill, inset 15, gap 0, wrap 4, debug", "[][][][]", "[][][][][][][][][][]"));
        this.setLayout(new MigLayout("fill", "[][][][]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        //this.setBackground(Application.lighter_turquoise);
        formContainer.setBackground(Color.white);
        formContainer.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[grow 50][grow 50]", "[][][][][][][]push"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(titleFont);
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/patient-info64-2.png")));
        add(title, "cell 0 0 4 1, alignx left");

        //add(formContainer,  "cell 0 1 4 8, grow, gap 10");
        //place in column 0, row 1, expand to 4 columns and 8 rows. Gap 10px left and right
        add(formContainer,  "cell 0 1 4 8, grow, gap 10 10");

        //add(title1, "cell 0 0, grow");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        formContainer.add(nameHeading, "cell 0 0");
        //add(nameText, "skip 1, grow");

        surnameHeading = new JLabel("Surname*");
        surnameHeading.setFont(contentFont);
        surnameHeading.setForeground(contentColor);
        formContainer.add(surnameHeading, "grow");

        //ROW 2
        formContainer.add(name, "grow");
        formContainer.add(surname, "grow");

        //ROW 3
        sexHeading = new JLabel("Sex*");
        sexHeading.setFont(contentFont);
        sexHeading.setForeground(contentColor);
        formContainer.add(sexHeading, "grow");

        birthDateHeading = new JLabel("Date of Birth*");
        birthDateHeading.setFont(contentFont);
        birthDateHeading.setForeground(contentColor);
        formContainer.add(birthDateHeading, "grow");

        //ROW 4
        formContainer.add(sex, "grow");
        formContainer.add(birthDate,  "grow"); //TODO create birth date chooser

        //ROW 5
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");

        phoneHeading = new JLabel("Phone Number*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        formContainer.add(phoneHeading, "grow");

        //ROW 5
        formContainer.add(email, "grow");
        formContainer.add(phoneNumber, "grow");

        //Add buttons
        goBackButton = new MyButton("GO BACK", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        //add(goBackButton,"cell 1 7, left, gapx 10, gapy 5");
        add(goBackButton, "cell 0 9, span, center");

        applyChanges = new MyButton("APPLY");
        applyChanges.addActionListener(this);

        /*errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        //this.add(errorMessage, "cell 0 8, span, left");
        this.add(errorMessage, "cell 0 1, span, center");
        errorMessage.setVisible(true);*/

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            appMain.changeToPatientMenu();
        }
    }
}
