package pojos;

import ui.temp.SymptomCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Report {
    //private SymptomType symptomType;  // ArrayList of strings for symptoms
    private List<SymptomType> symptomList;
    private LocalDate date;
    // Constructor
    public Report() {
        symptomList = new ArrayList<>();
        //this.symptomType = SymptomType.None; // Initialize the ArrayList
        //this.date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
        this.date = LocalDate.now();
    }

    public Report(LocalDate date, SymptomType symptomType) {
        ArrayList<SymptomType> symptomTypeList = new ArrayList<>();
        //this.symptomType = symptomType; // Initialize the ArrayList
        this.date = date;
    }


    public String toString() {
        String sym = "";
        for (SymptomType symptomType : symptomList) {
            sym = sym.concat(symptomType.name());
        }
        return "Symptom{" +
                "date='" + getDate() + '\'' +  // Using inherited getter
                ", symptom=" + sym+
                '}';
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<SymptomType> getSymptomList() {
        return symptomList;
    }

    public void setSymptomList(List<SymptomType> symptomList) {
        this.symptomList = symptomList;
    }

    public void addSymptom(SymptomType symptomType) {
        symptomList.add(symptomType);
    }
}
