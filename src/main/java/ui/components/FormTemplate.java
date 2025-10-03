package ui.components;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

//import com.toedter.calendar.JDateChooser;

import javax.swing.BorderFactory;

public class FormTemplate extends JPanel implements ActionListener{

    private static final long serialVersionUID = 1L;
    private final Color titleColor2 = new Color(24, 116, 67);
    private final Color titleColor = new Color(7, 164, 121);
    private JLabel title;
    protected String titleText = " ";
    private JLabel option1;
    protected String option1Text = "    General details";
    private JLabel option2;
    protected String option2Text =  "    Medical Details";
    private JLabel option3;
    protected String option3Text = "    Diagnosis Details";
    private ArrayList<JLabel> optionTexts;
    private ArrayList<JPanel> forms;
    protected JPanel form1;
    protected JPanel form2;
    protected JPanel form3;
    protected JButton backButton;
    protected JButton nextButton;
    protected JButton deleteButton;
    protected JButton applyChanges;
    protected JButton goBackButton;
    private int panelShowed;
    protected JLabel errorMessage;
    protected JButton fromXMLButton;


    //Patient form components
    protected MyTextField name;
    protected MyTextField surname;
    protected MyComboBox<String> speciality;
    protected MyComboBox<String> sex;
    protected MyComboBox<String> emergency;
    protected MyComboBox<Integer> boxes;


    public FormTemplate() {
        //Initialize panel
    }

    protected void initPatientForm() {
        this.setLayout(new MigLayout("fill, inset 15, gap 0, wrap 4", "[][][][]", "[][][][][][][][][][]"));
        forms = new ArrayList<JPanel>();
        optionTexts = new ArrayList<JLabel>();
        panelShowed = 1;


        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/medical-chechup.png")));
        add(title, "cell 0 0 4 1, alignx left");

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



        }

        //Add buttons
        goBackButton = new MyButton("GO BACK");
        goBackButton.setBackground(new Color(7, 164, 121));
        goBackButton.setForeground(new Color(250, 250, 250));
        goBackButton.addActionListener(this);
        add(goBackButton,"cell 1 7, left, gapx 10, gapy 5");


        backButton = new MyButton("<");
        backButton.setBackground(new Color(7, 164, 121));
        backButton.setForeground(new Color(250, 250, 250));
        backButton.addActionListener(this);

        deleteButton = new MyButton("DELETE");
        deleteButton.setBackground(new Color(7, 164, 121));
        deleteButton.setForeground(new Color(250, 250, 250));
        deleteButton.addActionListener(this);
        add(deleteButton, "cell 2 7, center, gapy 5");
        deleteButton.setVisible(false);

        nextButton = new MyButton(">");
        nextButton.setBackground(new Color(7, 164, 121));
        nextButton.setForeground(new Color(250, 250, 250));
        nextButton.addActionListener(this);


        applyChanges = new MyButton("APPLY");
        applyChanges.setBackground(new Color(7, 164, 121));
        applyChanges.setForeground(new Color(250, 250, 250));
        applyChanges.addActionListener(this);

        if(panelShowed == forms.size()) {
            add(applyChanges, "cell 3 7, right, gapy 5");
        }else {
            add(nextButton, "cell 3 7, right, gapy 5");
            System.out.println("Panel showed: "+panelShowed+" forms size: "+forms.size());
        }

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 8, span, left");
        errorMessage.setVisible(false);

    }


    protected void initDoctorForm() {
        this.setLayout(new MigLayout("fill, inset 15, gap 0, wrap 4", "[][][][]", "[][][][][][][][][][]"));
        forms = new ArrayList<JPanel>();
        optionTexts = new ArrayList<JLabel>();
        panelShowed = 1;


        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/urgency/ui/icon/medical-chechup.png")));
        add(title, "cell 0 0 4 1, alignx left");

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



        }

        //Add buttons
        goBackButton = new MyButton("GO BACK");
        goBackButton.setBackground(new Color(7, 164, 121));
        goBackButton.setForeground(new Color(250, 250, 250));
        goBackButton.addActionListener(this);
        add(goBackButton,"cell 1 7, left, gapx 10, gapy 5");


        backButton = new MyButton("<");
        backButton.setBackground(new Color(7, 164, 121));
        backButton.setForeground(new Color(250, 250, 250));
        backButton.addActionListener(this);

        fromXMLButton = new MyButton("CREATE FROM XML");
        fromXMLButton.setBackground(new Color(7, 164, 121));
        fromXMLButton.setForeground(new Color(250, 250, 250));
        fromXMLButton.addActionListener(this);
        add(fromXMLButton, "cell 2 7, center, gapy 5");

        nextButton = new MyButton(">");
        nextButton.setBackground(new Color(7, 164, 121));
        nextButton.setForeground(new Color(250, 250, 250));
        nextButton.addActionListener(this);


        applyChanges = new MyButton("APPLY");
        applyChanges.setBackground(new Color(7, 164, 121));
        applyChanges.setForeground(new Color(250, 250, 250));
        applyChanges.addActionListener(this);

        if(panelShowed == forms.size()) {
            add(applyChanges, "cell 3 7, right, gapy 5");
        }else {
            add(nextButton, "cell 3 7, right, gapy 5");
            System.out.println("Panel showed: "+panelShowed+" forms size: "+forms.size());
        }

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 8, span, left");
        errorMessage.setVisible(false);

    }

    private void enableBackground(JLabel lbl) {
        lbl.setBackground(Color.WHITE);
        Border greenLine = BorderFactory.createLineBorder(titleColor);
        lbl.setBorder(greenLine);
        lbl.setOpaque(true);
    }
    private void disableBackground(JLabel lbl) {
        lbl.setOpaque(false);
        lbl.setBorder(null);
    }

    protected void showErrorMessage(String error) {
        errorMessage.setText(error);
        errorMessage.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== backButton) {
            if(panelShowed>1) {
                panelShowed--;
            }

            if(panelShowed == 1) {
                backButton.setVisible(false);
                this.remove(backButton);
                goBackButton.setVisible(true);
                add(goBackButton, "cell 1 7, left, gapx 10, gapy 5");
            }

            if(applyChanges.isVisible() && panelShowed != forms.size()) {
                applyChanges.setVisible(false);
                this.remove(applyChanges);
                nextButton.setVisible(true);
                add(nextButton, "cell 3 7, right, gapy 5");
            }
            changePanel();

        }else if(e.getSource() == nextButton) {
            panelShowed++;

            if(panelShowed != 1) {
                goBackButton.setVisible(false);
                this.remove(goBackButton);
                backButton.setVisible(true);
                add(backButton, "cell 1 7, left, gapx 10, gapy 5");
            }
            if(panelShowed == forms.size()) {
                nextButton.setVisible(false);
                this.remove(nextButton);
                applyChanges.setVisible(true);
                add(applyChanges, "cell 3 7, right, gapy 5");
            }


            if(!forms.get(0).isVisible()) {
                goBackButton.setVisible(false);
                this.remove(goBackButton);
                backButton.setVisible(true);
                add(backButton, "cell 1 7, left, gapx 10, gapy 5");
            }
            changePanel();
        }

    }


    protected void initPatientForm1(Boolean isVisible) {
        if(form1 == null) {
            //form1 = new FormPanel();
        }
        forms.add(form1);
        form1.setVisible(isVisible);

        if(isVisible) {
            add(form1,  "cell 1 1 3 6, grow, gap 10");
        }
    }

    protected void initPatientForm2(Boolean isVisible) {
        if(form2 == null) {
            //form2 = new FormPanel();
        }
        form2.setVisible(isVisible);
        forms.add(form2);

        if(isVisible) {
            add(form2,  "cell 1 1 3 6, grow, gap 10");
        }
    }
    protected void initPatientForm3(Boolean isVisible) {
        if(form3 == null) {
            //form3 = new FormPanel();
        }
        form3.setVisible(isVisible);
        forms.add(form3);

        if(isVisible) {
            add(form3,  "cell 1 1 3 6, grow, gap 10");
        }
    }

    protected void changePanel() {

        for (int i = 0; i<forms.size(); i++) {
            if(forms.get(i).isVisible()) {
                forms.get(i).setVisible(false);
                this.remove(forms.get(i));
                disableBackground(optionTexts.get(i));
            }
        }

        forms.get(panelShowed-1).setVisible(true);
        add(forms.get(panelShowed-1),  "cell 1 1 3 6, grow, gap 10");
        enableBackground(optionTexts.get(panelShowed-1));
    }

    protected void resetPanel() {
        this.removeAll();
        //forms.removeAll(forms);
        //optionTexts.removeAll(optionTexts);

        forms = null;
        optionTexts = null;
        panelShowed = 0;
    }


}
