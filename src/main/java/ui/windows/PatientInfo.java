package ui.windows;

import pojos.Patient;
import net.miginfocom.swing.MigLayout;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel that displays read-only personal information of the currently logged-in patient.
 * <p>
 * This view is accessed from the patient menu to show demographic and contact data.
 * The panel is not editable and acts purely as an information display.
 * </p>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>The panel is created once inside {@link PatientMenu} and reused for the entire session.</li>
 *     <li>{@link #initPatientInfo()} initializes Swing components and delegates layout to {@link #initPatientForm()}.</li>
 *     <li>Whenever the patient accesses this view, {@link #updatePatientForm(Patient)} is called
 *         to populate the fields with fresh data from the main application state.</li>
 *     <li>When the user exits, the panel simply switches back to the main menu without deleting data.</li>
 *     <li>No resetting is required, since all fields are overwritten each time the panel is shown.</li>
 * </ul>
 */
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
    private JLabel genderHeading;
    private MyTextField gender;
    private JLabel birthDateHeading;
    private MyTextField birthDate;

    private JLabel title;
    protected String titleText = " ";
    protected JButton applyChanges;
    protected JButton goBackButton;
    protected JPanel formContainer;


    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;
    private Color textFieldBg = new Color(230, 245, 241);

    /**
     * Creates the patient information panel and initializes the UI components.
     *
     * @param appMain reference to the main application, used for navigation
     *                and access to the current {@link Patient} instance.
     */
    public PatientInfo(Application appMain) {
        this.appMain = appMain;
        initPatientInfo();

    }

    /**
     * Initializes the underlying model and disabled text fields, then delegates the layout
     * construction to {@link #initPatientForm()}. This method is executed only once.
     */
    public void initPatientInfo() {
        this.titleText = "Patient information";

        //Initialize values
        name = new MyTextField();
        name.setEnabled(false); //Doesnt allow editing

        surname = new MyTextField();
        surname.setEnabled(false);

        email = new MyTextField();
        email.setEnabled(false);

        phoneNumber = new MyTextField();
        phoneNumber.setEnabled(false);

        gender = new MyTextField();
        gender.setEnabled(false);

        birthDate = new MyTextField();
        birthDate.setEnabled(false);

        formContainer = new JPanel();
        initPatientForm();
    }
    /**
     * Builds and lays out the structure of the panel, including:
     * <ul>
     *     <li>Title header</li>
     *     <li>A form with read-only text fields for all patient attributes</li>
     *     <li>Navigation button to return to the main menu</li>
     * </ul>
     * This method sets up the static visual layout; actual patient data is injected later through {@link #updatePatientForm(Patient)}.
     */
    private void initPatientForm() {
        this.setLayout(new MigLayout("fill", "[][][][]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
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
        add(formContainer,  "cell 0 1 4 8, grow, gap 10 10");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        formContainer.add(nameHeading, "cell 0 0");

        surnameHeading = new JLabel("Surname*");
        surnameHeading.setFont(contentFont);
        surnameHeading.setForeground(contentColor);
        formContainer.add(surnameHeading, "grow");

        //ROW 2
        formContainer.add(name, "grow");
        formContainer.add(surname, "grow");

        //ROW 3
        genderHeading = new JLabel("Sex*");
        genderHeading.setFont(contentFont);
        genderHeading.setForeground(contentColor);
        formContainer.add(genderHeading, "grow");

        birthDateHeading = new JLabel("Date of Birth*");
        birthDateHeading.setFont(contentFont);
        birthDateHeading.setForeground(contentColor);
        formContainer.add(birthDateHeading, "grow");

        //ROW 4
        formContainer.add(gender, "grow");
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
        add(goBackButton, "cell 0 9, span, center");

        applyChanges = new MyButton("APPLY");
        applyChanges.addActionListener(this);

    }
    /**
     * Updates the form fields with the values of the given patient.
     * <p>
     * Called every time the patient opens the "See My Details" section, ensuring
     * that the view always reflects the most up-to-date information received from the server.
     * </p>
     *
     * @param patient the patient whose information is displayed
     */
    public void updatePatientForm(Patient patient) {
        name.setText(patient.getName());
        surname.setText(patient.getSurname());
        email.setText(patient.getEmail());
        birthDate.setText(patient.getDateOfBirth().toString());
        phoneNumber.setText(Integer.toString(patient.getPhone()));
        gender.setText(patient.getGender());
    }
    /**
     * Clears all fields. While generally unused (since fields are overwritten
     * before each display), this method is useful if the panel is repurposed or recycled.
     */
    private void resetForm() {
        name.setText("");
        surname.setText("");
        email.setText("");
        birthDate.setText("");
        phoneNumber.setText("");
        gender.setText("");
    }

    /**
     * Handles UI actions:
     * <ul>
     *     <li><b>Go Back</b> — returns to the main menu using
     *         {@link Application#changeToMainMenu()}.</li>
     * </ul>
     *
     * @param e action event triggered by user interaction
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            resetForm();
            appMain.changeToMainMenu();
        }
    }
}
