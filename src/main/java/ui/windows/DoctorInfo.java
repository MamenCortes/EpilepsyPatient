package ui.windows;

import pojos.Doctor;
import net.miginfocom.swing.MigLayout;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel that displays detailed information about the patient's doctor.
 * <p>
 * This view is non-editable and is used strictly for displaying profile data.
 * It is reused across the application's lifecycle. Whenever the panel becomes
 * visible again, the controller must call {@link #updateDoctorForm(Doctor)} to refresh
 * displayed values.
 * </p>
 *
 * <h3>Lifecycle and Reuse</h3>
 * <ul>
 *     <li>The panel is instantiated once in {@code MainMenu}.</li>
 *     <li>When navigated to, {@code updateView()} is always invoked to reload
 *         the doctor’s information.</li>
 *     <li>When navigating back to the main menu, {@link #resetForm()} clears
 *         all displayed fields.</li>
 * </ul>
 *
 * @author MamenCortes
 */
public class DoctorInfo extends JPanel implements ActionListener {
    private Application appMain;
    private JLabel nameHeading;
    private MyTextField name;
    private JLabel emailHeading;
    private MyTextField email;
    private JLabel phoneHeading;
    private MyTextField phoneNumber;
    private JLabel specHeading;
    private MyTextField speciality;
    private JLabel departmentHeading;
    private MyTextField department;

    private JLabel title;
    protected String titleText = " ";
    protected JButton goBackButton;
    protected JLabel errorMessage;
    protected JPanel formContainer;

    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;

    /**
     * Creates the doctor information panel, initializes layout and UI components.
     * <p>
     * Actual values are not set at construction time; instead they are assigned
     * when {@link #updateDoctorForm(Doctor)} is called.
     * </p>
     *
     * @param appMain reference to the {@link Application} controller used for
     *                navigation and data access.
     */
    public DoctorInfo(Application appMain) {
        this.appMain = appMain;
        initDoctorInfo();
    }

    public void initDoctorInfo() {
        this.titleText = "Physician information";

        //Initialize values
        name = new MyTextField();
        name.setEnabled(false); //Doesnt allow editing

        email = new MyTextField();
        email.setEnabled(false);

        phoneNumber = new MyTextField();
        phoneNumber.setEnabled(false);

        speciality = new MyTextField();
        speciality.setEnabled(false);

        department = new MyTextField();
        department.setEnabled(false);

        formContainer = new JPanel();
        initDoctorForm();
    }
    /**
     * Initializes the panel structure, form layout and UI components for displaying doctor data:
     * name, email, phone number, specialty, and office.
     * <p>
     * This method only builds the UI; values remain empty until a doctor is
     * provided via {@link #updateDoctorForm(Doctor)}.
     * </p>
     */
    private void initDoctorForm() {
        this.setLayout(new MigLayout("fill", "[][][][]", "[][][][][][][][][][][]"));
        this.setBackground(Color.white);
        formContainer.setBackground(Color.white);
        formContainer.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[grow 10][grow 90]", "[][][][][]push"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(titleFont);
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/doctor-info64_2.png")));
        add(title, "cell 0 0 4 1, alignx left");
        add(formContainer, "cell 0 1 4 8, grow, gap 10 10");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        formContainer.add(nameHeading, "grow");

        //R
        formContainer.add(name, "grow");

        //ROW 3
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");
        formContainer.add(email, "grow");

        phoneHeading = new JLabel("Phone*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        formContainer.add(phoneHeading, "grow");
        formContainer.add(phoneNumber, "grow");

        //ROW 5
        specHeading = new JLabel("Speciality*");
        specHeading.setFont(contentFont);
        specHeading.setForeground(contentColor);
        formContainer.add(specHeading, "grow");
        //ROW 6
        formContainer.add(speciality, "grow");


        //ROW 7
        departmentHeading = new JLabel("Department*");
        departmentHeading.setFont(contentFont);
        departmentHeading.setForeground(contentColor);
        formContainer.add(departmentHeading, "grow");

        //ROW 8
        formContainer.add(department, "grow"); //TODO create birth date chooser

        //Add buttons
        goBackButton = new MyButton("GO BACK", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 10, span, center");

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 9, span, center");
        errorMessage.setVisible(false);

    }

    /**
     * Displays an error message.
     * @param message the error message
     */
    private void showErrorMessage(String message) {
        errorMessage.setVisible(true);
        errorMessage.setText(message);
    }

    /**
     * Updates the panel fields with the provided doctor information.
     * <p>
     * This method must be called every time the panel becomes visible to ensure
     * correct and up-to-date data is displayed.
     * It displays an error message if the Patient doesn't have a Doctor assigned
     * </p>
     *
     * @param doctor the doctor whose information will be displayed.
     */
    public void updateDoctorForm(Doctor doctor) {
        if(doctor != null) {
            name.setText(doctor.getName()+" "+doctor.getSurname());
            email.setText(doctor.getEmail());
            phoneNumber.setText(Integer.toString(doctor.getPhone()));
            speciality.setText(doctor.getSpeciality());
            department.setText(doctor.getDepartment());
        }else{
            showErrorMessage("No doctor assigned yet");
        }
        }

    /**
     * Clears all doctor information fields.
     * <p>
     * This is called automatically when navigating back to the main menu to
     * ensure the panel does not retain outdated data.
     * </p>
     */
    public void resetForm() {
        name.setText("");
        email.setText("");
        phoneNumber.setText("");
        speciality.setText("");
        department.setText("");
        errorMessage.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBackButton) {
            resetForm();
            appMain.changeToMainMenu();
        }
    }
}