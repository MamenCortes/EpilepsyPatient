package ui;

import net.miginfocom.swing.MigLayout;
import ui.components.FormTemplate;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;
//import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PatientInfo extends JPanel implements ActionListener {
    private Application appMain;
    private MyComboBox<Integer> emergencyCB;
    private MyTextField name;
    private MyTextField surname;
    private MyTextField email;
    private MyTextField phoneNumber;
    private MyTextField sex;
    private MyComboBox<String> nextStep;


    private final Color titleColor2 = new Color(24, 116, 67);
    private final Color titleColor = new Color(7, 164, 121);
    private JLabel title;
    protected String titleText = " ";
    protected JPanel form1;
    protected JButton backButton;
    protected JButton nextButton;
    protected JButton deleteButton;
    protected JButton applyChanges;
    protected JButton goBackButton;
    protected JLabel errorMessage;


    //private JDateChooser birthDate;
    public PatientInfo(Application appMain) {
        this.appMain = appMain;
        initPatientInfo();

    }

    public void initPatientInfo() {
        this.titleText = "Patient information";

        //Initialize values
        //TODO: replace with actual patient values
        name = new MyTextField();
        name.setText("Jane");
        name.setEnabled(false);
        surname = new MyTextField();
        surname.setText("Doe");
        surname.setEnabled(false);
        email = new MyTextField();
        email.setText("jane.doe@gmail.com");
        email.setEnabled(false);
        phoneNumber = new MyTextField();
        phoneNumber.setText("123456789");
        phoneNumber.setEnabled(false);
        sex = new MyTextField();
        sex.setText("Non Binary");
        sex.setEnabled(false);
        initPatientForm();
    }

    private void initPatientForm() {
        //this.setLayout(new MigLayout("fill, inset 15, gap 0, wrap 4, debug", "[][][][]", "[][][][][][][][][][]"));
        this.setLayout(new MigLayout("debug, fill", "[][][][]", "[][][][][][][][][][]"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/medical-chechup.png")));
        add(title, "cell 0 0 4 1, alignx left");

        /*
        //Add Options
        if(option1Text != null) {
            option1 = new JLabel(option1Text);
            option1.setForeground(titleColor2);
            option1.setFont(new Font("sansserif", 1, 12));
            optionTexts.add(option1);
            option1.setAlignmentX(CENTER_ALIGNMENT);

            if(optionTexts.get(0).equals(option1)) {
                add(option1, "cell 0 1, alignx center, grow");
                enableBackground(option1);
                initPatientForm1(true);
            }else {
                add(option1, "cell 0 "+(optionTexts.indexOf(option1)+1)+", alignx center, grow");
                //add(option1, "skip 3, alignx center, grow");
                initPatientForm1(false);
            }

        }


        if(option2Text != null) {
            option2 = new JLabel(option2Text);
            option2.setForeground(titleColor2);
            option2.setFont(new Font("sansserif", 1, 12));
            option2.setAlignmentX(CENTER_ALIGNMENT);
            optionTexts.add(option2);
            //forms.add(form2);

            if(optionTexts.get(0).equals(option2)) {
                add(option2, "cell 0 1, alignx center, grow");
                enableBackground(option2);
                initPatientForm2(true);
            }else {
                add(option2, "cell 0 "+(optionTexts.indexOf(option2)+1)+", alignx center, grow");
                //add(option2, "skip 3, alignx center, grow");
                initPatientForm2(false);
            }
        }


        if(option3Text != null) {
            option3 = new JLabel(option3Text);
            option3.setForeground(titleColor2);
            option3.setFont(new Font("sansserif", 1, 12));
            option3.setAlignmentX(CENTER_ALIGNMENT);
            optionTexts.add(option3);

            if(optionTexts.get(0).equals(option3)) {
                add(option3, "cell 0 1, alignx center, grow");
                enableBackground(option3);
                initPatientForm3(true);
            }else {
                add(option3, "cell 0 "+(optionTexts.indexOf(option3)+1)+", alignx center, grow");
                initPatientForm3(false);
            }



        }*/

        //Add buttons
        goBackButton = new MyButton("GO BACK");
        goBackButton.setBackground(new Color(7, 164, 121));
        goBackButton.setForeground(new Color(250, 250, 250));
        goBackButton.addActionListener(this);
        //add(goBackButton,"cell 1 7, left, gapx 10, gapy 5");
        add(goBackButton, "cell 0 9, span, center");

        applyChanges = new MyButton("APPLY");
        applyChanges.setBackground(new Color(7, 164, 121));
        applyChanges.setForeground(new Color(250, 250, 250));
        applyChanges.addActionListener(this);

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        //this.add(errorMessage, "cell 0 8, span, left");
        this.add(errorMessage, "cell 0 1, span, center");
        errorMessage.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
