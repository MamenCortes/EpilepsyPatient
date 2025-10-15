package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Symptoms extends Report{
    private ArrayList<String> symptoms;  // ArrayList of strings for symptoms
    // Constructor
    public Symptoms() {
        super();
        this.symptoms = new ArrayList<>();  // Initialize the ArrayList
    }
    // Getter for symptoms
    public ArrayList<String> getSymptoms() {
        return symptoms;  // Return as List for broader compatibility
    }
    // Setter for symptoms
    public void setSymptoms(ArrayList<String> symptoms) {
        this.symptoms = symptoms;
    }
    // Additional method to add a symptom for convenience
    public void addSymptom(String symptom) {
        this.symptoms.add(symptom);
    }
    @Override
    public String toString() {
        return "Symptoms{" +
                "date='" + getDate() + '\'' +  // Using inherited getter
                ", symptoms=" + symptoms +
                '}';
    }
}
