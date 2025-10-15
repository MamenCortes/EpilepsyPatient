package ui.temp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;

public class SymptomCalendar {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SymptomCalendar().createAndShowGUI());
    }

    // Example symptom data (for multiple months)
    private final List<Symptom> allSymptoms = List.of(
            new Symptom(LocalDate.of(2025, 10, 1), "Headache"),
            new Symptom(LocalDate.of(2025, 10, 2), "Nausea"),
            new Symptom(LocalDate.of(2025, 10, 2), "Dizziness"),
            new Symptom(LocalDate.of(2025, 11, 5), "Headache"),
            new Symptom(LocalDate.of(2025, 11, 6), "Fatigue")
    );

    private final Map<String, Color> symptomColors = Map.of(
            "Headache", Color.RED,
            "Nausea", Color.ORANGE,
            "Dizziness", Color.YELLOW,
            "Fatigue", Color.CYAN
    );

    private JTable table;
    private JPanel legendPanel;

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Symptom Calendar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Month selection combobox
        String[] months = Arrays.stream(Month.values())
                .map(Month::name)
                .toArray(String[]::new);
        JComboBox<String> monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthCombo.addActionListener(e -> updateTable(monthCombo.getSelectedIndex() + 1));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select month:"));
        topPanel.add(monthCombo);
        frame.add(topPanel, BorderLayout.NORTH);

        // Initial table
        table = new JTable();
        table.setRowHeight(65);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // Legend panel
        legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));
        for (Map.Entry<String, Color> entry : symptomColors.entrySet()) {
            JPanel colorBox = new JPanel();
            colorBox.setBackground(entry.getValue());
            colorBox.setPreferredSize(new Dimension(20, 20));
            JLabel label = new JLabel(" " + entry.getKey() + " ");
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
            item.add(colorBox);
            item.add(label);
            legendPanel.add(item);
        }
        frame.add(legendPanel, BorderLayout.SOUTH);

        // Populate table for the initially selected month
        updateTable(monthCombo.getSelectedIndex() + 1);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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

        outer:
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 0 && col < startCol) continue;
                if (dayCounter > daysInMonth) break outer;

                LocalDate currentDate = LocalDate.of(year, month, dayCounter);

                // Prepara el texto de la celda: "día:síntoma1,síntoma2"
                StringBuilder cellText = new StringBuilder();
                cellText.append(dayCounter); // siempre ponemos el número del día
                StringBuilder symptomsPart = new StringBuilder();
                for (Symptom s : allSymptoms) {
                    if (s.getDate().equals(currentDate)) {
                        if (symptomsPart.length() > 0) symptomsPart.append(",");
                        symptomsPart.append(s.getName());
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
        table.setDefaultRenderer(Object.class, new SymptomCellRenderer(symptomColors));
    }

    // Symptom class
    static class Symptom {
        private final LocalDate date;
        private final String name;

        public Symptom(LocalDate date, String name) {
            this.date = date;
            this.name = name;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getName() {
            return name;
        }
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
}





