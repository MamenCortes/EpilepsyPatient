package ui.windows;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.stream.Collectors;

import pojos.Signal;
import net.miginfocom.swing.MigLayout;
import ui.components.MyButton;
import ui.components.MyTextField;
import ui.components.RecordingCell;

import javax.swing.*;
/**
 * Panel that displays the full list of physiological signal recordings belonging
 * to the currently logged-in patient.
 * <p>
 * The user may search recordings by date, reset the search results, or navigate
 * back to the patient menu. Each item in the list represents a {@link Signal}.
 * The graph visualization of the signals is not supported. The patient can only see the Signal metadata.
 * </p>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>Created once in {@link PatientMenu} and reused each time the patient enters the section.</li>
 *     <li>{@link #initMainPanel()} constructs the static UI layout (title, search bar, list container).</li>
 *     <li>{@link #updateSignalRecordingsList(List)} is called every time the menu navigates to this view,
 *         ensuring fresh data is displayed.</li>
 *     <li>When the panel is exited (via “Back to Menu”), {@link #resetPanel()} clears search fields
 *         and internal lists to prepare for the next visit.</li>
 * </ul>
 */
public class RecordingsHistory extends JPanel implements ActionListener, MouseListener {

    private static final long serialVersionUID = -2213334704230710767L;
    private Application appMain;
    protected final Font titleFont = new Font("sansserif", 3, 15);
    protected final Color titleColor = Application.dark_purple;
    protected JLabel title;
    protected String titleText = " Recordings History";
    protected ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/search-report64_2.png"));
    protected JScrollPane scrollPane1;
    protected String searchText = "Search By Date";
    protected MyTextField searchByTextField;
    protected MyButton searchButton;
    protected MyButton resetListButton;
    protected JLabel errorMessage;
    protected MyButton goBackButton;
    protected JList<Signal> reportsList;
    protected DefaultListModel<Signal> recordingsDefListModel;
    private List<Signal> allRecordings;
    /**
     * Creates the panel and initializes the user interface elements.
     *
     * @param appMain reference to the main application controller. Used for navigation
     *                and to access patient data and server responses.
     */
    public RecordingsHistory(Application appMain) {
        this.appMain = appMain;
        initMainPanel();
    }
    /**
     * Initializes and lays out the search bar, title header, results list and navigation button.
     * <p>
     * This method is called only once in the constructor. Dynamic content such as the list
     * of recordings is populated later through {@link #updateSignalRecordingsList(List)}.
     * </p>
     */
    private void initMainPanel() {
        this.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 3", "[grow 5]5[grow 5]5[grow 40][grow 40]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(icon);
        add(title, "cell 0 0 3 1, alignx left");

        //Initialize search panel
        JLabel searchTitle = new JLabel(searchText);
        searchTitle.setFont(titleFont);
        searchTitle.setForeground(Application.darker_purple);
        add(searchTitle, "cell 0 1 2 1, alignx center, grow");

        searchByTextField = new MyTextField("ex. Doe...");
        searchByTextField.setBackground(Application.lighter_turquoise);
        searchByTextField.setHint("YYYY-MM-DD");
        add(searchByTextField, "cell 0 2 2 1, alignx center, grow");

        resetListButton = new MyButton("RESET");
        resetListButton.addActionListener(this);
        add(resetListButton, "cell 0 3, left, gapy 5, grow");

        searchButton = new MyButton("SEARCH");
        searchButton.addActionListener(this);
        add(searchButton, "cell 1 3, right, gapy 5, grow");

        goBackButton = new MyButton("BACK TO MENU", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 7, center, gapy 5, span 2, grow");
        goBackButton.setVisible(true);

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 5, span 2, left");
        errorMessage.setVisible(false);

        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setOpaque(false);
        scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        recordingsDefListModel = new DefaultListModel<Signal>();
        reportsList = new JList<Signal>(recordingsDefListModel);
        reportsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsList.setCellRenderer(new RecordingCell());
        reportsList.addMouseListener(this);
        scrollPane1.setViewportView(reportsList);
        scrollPane1.setPreferredSize(this.getPreferredSize());

        add(scrollPane1, "cell 2 1 2 6, grow, gap 10");
    }
    /**
     * Updates the list model with a new set of recordings retrieved from the server.
     * <p>
     * It also caches the full list in {@code allRecordings} so the search functionality
     * can filter results without making additional network requests.
     * </p>
     *
     * @param list list of signals to display; may be empty or null
     */
    public void updateSignalRecordingsList(List<Signal> list){
        if(list == null || list.isEmpty()) {
            showErrorMessage("No signal found!");
            //openRecordingButton.setVisible(false);
        }else{
            if(allRecordings == null) {
                allRecordings = list;
            }
        }
        recordingsDefListModel.removeAllElements();
        for (Signal r : list) {
            recordingsDefListModel.addElement(r);

        }
    }
    /**
     * Shows an error message below the search panel, used when filtering or loading fails.
     *
     * @param message the text to display
     */
    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    /**
     * Hides the error message label.
     */
    private void hideErrorMessage() {
        errorMessage.setVisible(false);
    }
    /**
     * Resets the panel to its default state:
     * <ul>
     *     <li>Clears search text</li>
     *     <li>Clears the stored list of recordings</li>
     *     <li>Clears the displayed list model</li>
     *     <li>Hides error messages</li>
     * </ul>
     * <p>
     * This method is invoked whenever the user leaves the panel
     * (e.g., by clicking “Back to Menu”).
     * </p>
     */
    private void resetPanel(){
        hideErrorMessage();
        searchByTextField.setText("");
        allRecordings = null;
        recordingsDefListModel.clear();
    }

    /**
     * Handles the button interactions:
     * <ul>
     *     <li><b>Back to Menu</b> – Resets the panel and returns to the main menu.</li>
     *     <li><b>Search</b> – Filters recordings by date (substring match).</li>
     *     <li><b>Reset</b> – Restores the full original recordings list.</li>
     * </ul>
     *
     * @param e the event triggered by button clicks
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            resetPanel();
            appMain.changeToMainMenu();
        }if(e.getSource() == searchButton) {
            errorMessage.setVisible(false);
            String input = searchByTextField.getText();
            System.out.println(input);
            List<Signal> filteredRecordings = allRecordings.stream()
                    .filter(r -> r.getDate().toString().contains(input))
                    .collect(Collectors.toList());

            updateSignalRecordingsList(filteredRecordings);
            if(filteredRecordings.isEmpty()) {
                showErrorMessage("No Signals found");
            }
        }else if(e.getSource() == resetListButton) {
            updateSignalRecordingsList(allRecordings);
            if (allRecordings.isEmpty()) {
                showErrorMessage("No patient found");
            }
        }

    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}