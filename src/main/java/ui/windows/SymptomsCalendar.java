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

    public SymptomsCalendar(Application appMenu) {
        this.appMenu = appMenu;
        colors = generateSymptomColors(SymptomType.class);
        //TODO: get real patient symptoms
        allSymptoms = ModelManager.generateRandomSymptomReports();
        System.out.println("Num symptoms: " + allSymptoms.size());
        initPanel();
    }
    public static void main(String[] args) {
        //SwingUtilities.invokeLater(() -> new SymptomCalendar().createAndShowGUI());
        SymptomsCalendar symptomsCalendar = new SymptomsCalendar(null);
    }

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

        //
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
        //showPatients(appMain.patientMan.searchPatientsBySurname("Blanco"));
        //showDoctors(createRandomDoctors());

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
        //legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

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
        //legendScroll.setPreferredSize(new Dimension(200, 120)); // ajusta tamaño según necesites
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

    private void updateTable(int month) {
        int year = LocalDate.now().getYear();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        DayOfWeek firstWeekday = firstDay.getDayOfWeek();

        String[] columns = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[][] data = new String[6][7]; // max 6 weeks

        int dayCounter = 1;
        int startCol = firstWeekday.getValue() % 7; // Sunday=0

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        outer:
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 0 && col < startCol) continue;
                if (dayCounter > daysInMonth) break outer;

                LocalDate currentDate = LocalDate.of(year, month, dayCounter);
                StringBuilder cellText = new StringBuilder();
                cellText.append(dayCounter); // siempre mostramos el número del día

                // Buscar síntomas de este día
                StringBuilder symptomsPart = new StringBuilder();
                for (Report s : allSymptoms) {
                    LocalDate symptomDate = LocalDate.parse(s.getDate(), formatter);
                    if (symptomDate.equals(currentDate)) {
                        if (symptomsPart.length() > 0) symptomsPart.append(",");
                        symptomsPart.append(s.getSymptomType().name()); // si es enum
                    }
                }

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

    // Custom renderer
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

            /*if (isSelected) {
                setBackground(new Color(200, 220, 255));
            }*/

            return this;
        }
    }

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
