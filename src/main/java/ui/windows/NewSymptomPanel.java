package ui.windows;

import net.miginfocom.swing.MigLayout;
import network.ServerError;
import pojos.Report;
import pojos.SymptomType;
import ui.components.MyButton;
import ui.components.MyTextField;
import ui.components.MyToggleButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NewSymptomPanel extends JPanel implements ActionListener {
    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Font subHeadingFont = new Font("sansserif", 1, 15);
    private final Color contentColor = Application.dark_turquoise;
    private Color textFieldBg = new Color(230, 245, 241);

    //Components
    private ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/search-report64_2.png"));
    private MyButton save;
    private MyButton cancel;
    private JLabel errorMessage;
    private ArrayList<JToggleButton> buttons;
    private MyTextField dateTxtField;

    //
    private Application appMain;
    private Report report;

    public NewSymptomPanel(Application appMain) {
        //this.appMain = appMain;
        this.appMain = appMain;
        buttons = new ArrayList<>();
        initPanel();
        report=new Report();


    }

    private void initPanel() {
        this.setLayout(new MigLayout("fill, inset 20", "[25%][25%][25%][25%]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        //Add Title
        JLabel title = new JLabel("New Symptoms");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(icon);
        add(title, "cell 0 0 4 1, alignx left");

        JLabel text = new JLabel("Select all the symptoms you are experiencing:");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        text.setForeground(contentColor);
        text.setFont(subHeadingFont);
        add(text, "cell 0 1 4 1, alignx center");

        JPanel interiorPanel = new JPanel();
        interiorPanel.setBackground(Color.white);
        interiorPanel.setLayout(new MigLayout(
                "inset 10, wrap 2",  // 3 columnas visibles
                "[grow][grow]",  // cada columna se expande igual
                ""                     // filas automáticas
        ));


        for (SymptomType symptomType : SymptomType.values()) {
            if(symptomType != SymptomType.None) {
                String symptomTypeName = symptomType.name().replace("_", " ");
                MyToggleButton symptomTypeButton = new MyToggleButton(symptomTypeName);
                interiorPanel.add(symptomTypeButton, "growx");
                buttons.add(symptomTypeButton);
            }
        }

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setForeground(contentColor);
        dateLabel.setFont(subHeadingFont);
        add(dateLabel, "cell 1 2, alignx center");

        dateTxtField= new MyTextField("YYYY-MM-DD");
        dateTxtField.setText(LocalDate.now().toString());
        add(dateTxtField, "cell 2 2, alignx center, growx");

        JScrollPane scrollPane = new JScrollPane(interiorPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, "cell 0 3 4 3, alignx center, growx");
        //add(scrollPane, "cell 0 2 3 3, alignx center");

        errorMessage = new JLabel("Error");
        errorMessage.setForeground(Color.RED);
        errorMessage.setFont(contentFont);
        errorMessage.setVisible(false);
        add(errorMessage, "cell 0 6 4 1, alignx center");

        save = new MyButton("SAVE", Application.turquoise, Color.white);
        save.addActionListener(this);
        cancel = new MyButton("CANCEL", Application.turquoise, Color.white);
        cancel.addActionListener(this);
        add(save, "cell 1 7, alignx center, growx");
        add(cancel, "cell 2 7, alignx center, growx");
    }

    private void showErrorMessage(String message){
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == save) {
            LocalDate date;
            Report report = new Report();
            try {
                date = LocalDate.parse(dateTxtField.getText());
                report.setDate(date);
            }catch (DateTimeParseException ex){
                showErrorMessage("Invalid date");
                return;
            }
            if(date == null) {showErrorMessage("Invalid date");}
            int count = 0;
            for(JToggleButton button : buttons) {
                if(button.isSelected()) {
                    count++;
                    String text = button.getText().replace(" ", "_");
                    report.addSymptom(SymptomType.valueOf(text));
                }
            }

            if(count == 0) {
                showErrorMessage("No symptoms selected");
                return;
            }

            try{
                appMain.client.sendReport(report, appMain.patient.getId(), appMain.user.getId());
                appMain.patient.addSymptom(report);
                System.out.println(report);
                resetPanel();
                appMain.changeToMainMenu();
            }catch(ServerError | IOException | InterruptedException ex){
                showErrorMessage("Could not save the symptoms. Please try again");
            }

        }else if(e.getSource() == cancel) {
            appMain.changeToMainMenu();
        }
    }

    private void resetPanel() {
        errorMessage.setVisible(false);
        //report.setSymptomList(new ArrayList<>());
        dateTxtField.setText(LocalDate.now().toString());
        for(JToggleButton button : buttons) {
            if(button.isSelected()) {
                button.getModel().setSelected(false);
            }
        }
    }
}
