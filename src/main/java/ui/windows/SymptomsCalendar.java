package ui.windows;

import pojos.ModelManager;
import pojos.Report;
import pojos.SymptomType;
import net.miginfocom.swing.MigLayout;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.temp.SymptomCalendar;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Panel that displays a monthly calendar with all symptoms reported by the patient.
 * <p>
 * The calendar shows each day of the selected month, optionally including color-coded
 * markers representing the types of symptoms reported by the patient on that date.
 * </p>
 *
 * <h3>Features</h3>
 * <ul>
 *     <li>Selection of month and year</li>
 *     <li>Automatic refresh of the table when the month changes</li>
 *     <li>Color-coded legend that maps each {@link SymptomType} to a unique color</li>
 *     <li>Tooltip on each calendar cell listing the symptoms of that day</li>
 *     <li>Navigation back to the main menu</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>The panel is created once in {@link PatientMenu} and reused during the session.</li>
 *     <li>Data is initially loaded from {@code appMenu.patient.getSymptomsList()}.</li>
 *     <li>{@link #updateData(ArrayList)} is called whenever the patient menu opens this view
 *         to ensure the latest symptom reports are displayed.</li>
 *     <li>When the user selects a new month from the dropdown, {@link #updateTable(int)} recalculates
 *         the grid and repaints symptom markers.</li>
 * </ul>
 *
 * @author MamenCortes
 */
public class SymptomsCalendar extends JPanel implements ActionListener, MouseListener {

    private Map<String, Color> colors;
    private JTable table;
    private JPanel legendPanel;
    private Application appMenu;
    private ArrayList<Report> allSymptoms;

    //Format variables
    protected final Font titleFont = new Font("sansserif", 3, 15);
    protected final Color titleColor = Application.dark_purple;
    protected String titleText = " Symptoms History";
    protected ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/search-report64_2.png"));

    //Components
    private MyButton goBackButton;
    private MyComboBox<String> monthComboBox;
    private MyComboBox<String> yearComboBox;
    /**
     * Constructs the symptoms calendar and loads colors and initial symptom data.
     *
     * @param appMenu main application controller, used for navigation and access to patient reports
     */
    public SymptomsCalendar(Application appMenu) {
        this.appMenu = appMenu;
        colors = generateSymptomColors(SymptomType.class);
        allSymptoms = appMenu.patient.getSymptomsList();
        System.out.println("Num symptoms: " + allSymptoms.size());
        initPanel();
    }
    public static void main(String[] args) {
        SymptomsCalendar symptomsCalendar = new SymptomsCalendar(null);
    }
    /**
     * Generates a unique color for each symptom type. Used both for the legend and
     * for drawing colored markers in calendar cells.
     *
     * @param enumClass SymptomType enum
     * @return map from symptom name to its assigned color
     */
    public static Map<String, Color> generateSymptomColors(Class<? extends Enum<?>> enumClass) {
        Map<String, Color> colorMap = new HashMap<>();
        Random random = new Random();
        Set<Color> usedColors = new HashSet<>();

        for (Enum<?> constant : enumClass.getEnumConstants()) {
            Color color;
            do {
                color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            } while (usedColors.contains(color)); // evita duplicados exactos

            usedColors.add(color);
            colorMap.put(constant.name(), color);
        }

        return colorMap;
    }
    /**
     * Initializes the full panel layout including:
     * <ul>
     *     <li>Title header</li>
     *     <li>Month (and potentially year) selectors</li>
     *     <li>Calendar table</li>
     *     <li>Scrollable legend showing symptom colors</li>
     *     <li>Back to menu button</li>
     * </ul>
     * The table is initially populated with the current month via {@link #updateTable(int)}.
     */
    private void initPanel() {
        this.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 3", "[grow 5]5[grow 5]5[grow 90]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        //Add Title
        JLabel title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(icon);
        add(title, "cell 0 0 3 1, alignx left");

        JLabel monthHeading = new JLabel("Select a month:");
        monthHeading.setFont(titleFont);
        monthHeading.setForeground(Application.darker_purple);
        add(monthHeading, "cell 0 1 2 1, alignx center, grow");

        String[] months = Arrays.stream(Month.values())
                .map(Month::name)
                .toArray(String[]::new);
        monthComboBox = new MyComboBox<>();
        monthComboBox.setModel(new DefaultComboBoxModel<>(months));
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthComboBox.addActionListener(e -> updateTable(monthComboBox.getSelectedIndex() + 1));
        add(monthComboBox, "cell 0 2 2 1, alignx center, grow");

        // Initial table
        table = new JTable();
        table.setRowHeight(65);
        add(new JScrollPane(table), "cell 2 1, span 1 7, grow");

        // Legend panel (contenido desplazable)
        legendPanel = new JPanel();
        legendPanel.setBackground(Color.white);
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        // Create a white line border
        TitledBorder border = BorderFactory.createTitledBorder("Legend");
        border.setTitleFont(titleFont);
        border.setTitleColor(Application.turquoise);
        legendPanel.setBorder(border);

        // Añadimos los síntomas con sus colores
        for (Map.Entry<String, Color> entry : colors.entrySet()) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            item.setBackground(Color.white);

            JPanel colorBox = new JPanel();
            colorBox.setBackground(entry.getValue());
            colorBox.setPreferredSize(new Dimension(20, 20));

            JLabel label = new JLabel(" " + entry.getKey() + " ");
            item.add(colorBox);
            item.add(label);

            legendPanel.add(item);
        }

        // Crear scrollpane para la leyenda
        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBackground(Color.white);
        legendScroll.setBorder(null);
        legendScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        legendScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Añadir el scrollpane en el layout principal
        add(legendScroll, "cell 0 3 2 1, grow, gapy 5");

        goBackButton = new MyButton("BACK TO MENU", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 7, center, gapy 5, span 2, grow");
        goBackButton.setVisible(true);

        // Populate table for the initially selected month
        updateTable(monthComboBox.getSelectedIndex() + 1);
    }
    /**
     * Updates the loaded symptom reports with freshly retrieved data from the server
     * (or updated local model), and refreshes the calendar using the current month.
     *
     * @param reports the list of symptom reports for the current patient
     */
    public void updateData(ArrayList<Report> reports) {
        allSymptoms = reports;
        updateTable(LocalDate.now().getMonthValue());
    }

    /**
     * Reconstructs the calendar grid for the selected month.
     * <p>
     * For each day:
     * <ul>
     *     <li>Places the day number in the cell</li>
     *     <li>Searches for symptom reports for that date</li>
     *     <li>If found, appends the symptom identifiers after ":"</li>
     *     <li>The cell renderer (see {@link SymptomsCalendar.SymptomCellRenderer}) draws
     *         the colored squares corresponding to each symptom type</li>
     * </ul>
     * </p>
     *
     * @param month the selected month (1–12)
     */
    public void updateTable(int month) {
        int year = LocalDate.now().getYear();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        DayOfWeek firstWeekday = firstDay.getDayOfWeek();

        String[] columns = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[][] data = new String[6][7]; // max 6 weeks

        int dayCounter = 1;
        int startCol = firstWeekday.getValue() % 7; // Sunday=0

        outer:
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {

                if (row == 0 && col < startCol) continue;
                if (dayCounter > daysInMonth) break outer;

                LocalDate currentDate = LocalDate.of(year, month, dayCounter);

                StringBuilder cellText = new StringBuilder();
                cellText.append(dayCounter); // day number always shown

                // ---- Find symptoms for this date ----
                StringBuilder symptomsPart = new StringBuilder();

                for (Report r : allSymptoms) {
                    if (r.getDate().equals(currentDate)) {
                        // Loop through the list of symptom types
                        for (SymptomType type : r.getSymptomList()) {
                            if (symptomsPart.length() > 0) symptomsPart.append(",");
                            symptomsPart.append(type.name());
                        }
                    }
                }

                // If symptoms found, append them
                if (symptomsPart.length() > 0) {
                    cellText.append(":").append(symptomsPart);
                }

                data[row][col] = cellText.toString();
                dayCounter++;
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columns);
        table.setModel(model);

        // Custom cell renderer para mostrar número + cuadros de colores
        table.setDefaultRenderer(Object.class, new SymptomCalendar.SymptomCellRenderer(colors));
    }

    /**
     * Table cell renderer that displays:
     * <ul>
     *     <li>The day number at the top-left</li>
     *     <li>A row of small colored boxes representing symptoms reported that day</li>
     *     <li>A tooltip listing the symptom names when scrolling over them with the mouse</li>
     * </ul>
     */
    static class SymptomCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final Map<String, Color> symptomColors;
        private JLabel dayLabel;

        public SymptomCellRenderer(Map<String, Color> symptomColors) {
            this.symptomColors = symptomColors;
            setOpaque(true);
            setLayout(new BorderLayout());
            dayLabel = new JLabel();
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 12f));
            dayLabel.setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(Color.WHITE);
            setLayout(new BorderLayout());

            dayLabel = new JLabel();
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 12f));
            dayLabel.setHorizontalAlignment(SwingConstants.LEFT);

            if (value != null && !value.toString().isEmpty()) {
                String cellText = value.toString();
                String[] parts = cellText.split(":", 2);
                String dayPart = parts[0];
                String symptomPart = parts.length > 1 ? parts[1] : "";

                dayLabel.setText(dayPart);
                add(dayLabel, BorderLayout.NORTH);

                if (!symptomPart.isEmpty()) {
                    String[] symptoms = symptomPart.split(",");
                    JPanel symptomsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
                    for (String symptom : symptoms) {
                        JPanel box = new JPanel();
                        box.setBackground(symptomColors.getOrDefault(symptom, Color.LIGHT_GRAY));
                        box.setPreferredSize(new Dimension(15, 15));
                        symptomsPanel.add(box);
                    }
                    symptomsPanel.setBackground(Color.WHITE);
                    add(symptomsPanel, BorderLayout.CENTER);
                    setToolTipText(String.join(", ", symptoms));
                }
            }

            return this;
        }
    }

    /**
     * Navigates back to the main menu when the user clicks the corresponding button.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            appMenu.changeToMainMenu();
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
