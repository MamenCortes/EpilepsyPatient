package pojos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ui.temp.SymptomCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Report {
    //private SymptomType symptomType;  // ArrayList of strings for symptoms
    private List<SymptomType> symptomList;
    private LocalDate date;
    private int id;
    // Constructor
    public Report() {
        symptomList = new ArrayList<>();
        //this.symptomType = SymptomType.None; // Initialize the ArrayList
        //this.date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
        this.date = LocalDate.now();
        this.id = -1;
    }

    public Report(int id, LocalDate date, List<SymptomType> symptoms) {
        this.date = date;
        this.id = id;
        this.symptomList = symptoms;
    }


    public String toString() {
        String sym = "";
        for (SymptomType symptomType : symptomList) {
            sym = sym.concat(symptomType.name());
        }
        return "Symptom{" +
                "id=" + id +
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Converts this {@code Report} into a {@link JsonObject}.
     *
     * @return  a JSON representation of this report
     *
     * @see JsonObject
     */
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("date", date.toString());
        JsonArray symptomsJsonArray = new JsonArray();
        for(SymptomType symptom : symptomList) {
            symptomsJsonArray.add(symptom.name());
        }
        jsonObject.add("symptoms", symptomsJsonArray);
        return jsonObject;
    }

    /**
     * Creates a new {@code Report} instance from a {@link JsonObject}
     *
     * @param jsonObject  the JSON object containing this {@code Report} data
     * @return  a {@code Report} instance from the {@link JsonObject}
     *
     * @see JsonObject
     */
    public static Report fromJson(JsonObject jsonObject) {
        Report report = new Report();
        report.setId(jsonObject.get("id").getAsInt());
        report.setDate(LocalDate.parse(jsonObject.get("date").getAsString()));
        JsonArray symptomsJsonArray = jsonObject.get("symptoms").getAsJsonArray();
        List<SymptomType> symptoms = new ArrayList<>();
        for(JsonElement elem : symptomsJsonArray) {
            symptoms.add(SymptomType.valueOf(elem.getAsString()));
        }
        report.setSymptomList(symptoms);
        return report;
    }
}
