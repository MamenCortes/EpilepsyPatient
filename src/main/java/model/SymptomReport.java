package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SymptomReport {
    private SymptomType symptomType;  // ArrayList of strings for symptoms
    private String date;
    // Constructor
    public SymptomReport() {
        this.symptomType = SymptomType.None; // Initialize the ArrayList
        this.date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
    }

    public SymptomReport(String date,  SymptomType symptomType) {
        this.symptomType = symptomType; // Initialize the ArrayList
        this.date = date;
    }

    public SymptomType getSymptomType() {
        return symptomType;
    }

    public void setSymptomType(SymptomType symptomType) {
        this.symptomType = symptomType;
    }

    public String toString() {
        return "Symptom{" +
                "date='" + getDate() + '\'' +  // Using inherited getter
                ", symptom=" + symptomType +
                '}';
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
