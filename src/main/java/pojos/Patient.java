package pojos;

import com.google.gson.*;
import ui.temp.SymptomCalendar;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Patient} class represents a patient entity.
 * A {@code Patient} object encapsulates the necessary information for:
 * <ul>
 *     <li> The unique identifier of the patient</li>
 *     <li> The patient's name</li>
 *     <li> The patient's surname</li>
 *     <li> The patient's unique email address</li>
 *     <li> The patient's contact information</li>
 *     <li> The patient's date of birth</li>
 *     <li> The patient's gender</li>
 *     <li> Foreign key referencing the assigned {@link Doctor} (doctorId)</li>
 *     <li> List of {@code Signal} instances associated to the Patient</li>
 *     <li> List of {@code Report} intances associated to the Patient</li>
 * </ul>
 * <p>
 *     Instances of this class can be created either manually by the application or automatically
 *     reconstructed from database queries or from JSON files received over the network. Conversion utilities
 *     are provided inside the class {@link #fromJason(JsonObject)}
 * </p>
 * <p>
 *     An important point to consider is that certain field values will be validated externally when
 *     necessary (e.g., email) for correctness before being assigned to {@code Patient} instance.
 * </p>
 *
 * @author MamenCortes
 */
public class Patient {

    private Integer id;
    private String name;
    private String surname;
    private String email;
    private Integer phone;
    private String gender;
    private LocalDate dateOfBirth; //TODO: Change to LocalDate
    private int doctor_id;
    private ArrayList<Signal> signalRecordingsList;
    private ArrayList<Report> symptomsList;


    /**
     * Creates a {@code Patient} instance with all fields specified except for the id field to create
     * an entity.
     *
     * @param name      the patient's name
     * @param surname   the patient's surname
     * @param email     the patient's email address
     * @param phone     the patient's contact information (phone number)
     * @param gender    the patient's gender
     * @param dateOfBirth   the patient's date of birth
     */
    public Patient(String name, String surname, String email, Integer phone, String gender, LocalDate dateOfBirth) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.signalRecordingsList = new ArrayList<Signal>();
        this.symptomsList = new ArrayList<Report>();
    }

    /**
     * Creates a default {@code Patient} with field values either specified or empty.
     */
    public Patient() {
        this.name = "";
        this.surname = "";
        this.email = "";
        this.phone = 123456789;
        this.gender = "NonBinary";
        this.dateOfBirth = LocalDate.of(1900, 1, 1);
        this.signalRecordingsList = new ArrayList<Signal>();
        this.symptomsList = new ArrayList<Report>();
        this.id = 0;
        this.doctor_id = 0;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id + '\''+
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                ", sex='" + gender + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", numReports='" + signalRecordingsList.size() + '\'' +
                ", numSymptoms='" + symptomsList.size() + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public ArrayList<Signal> getSignalRecordingsList() {
        return signalRecordingsList;
    }

    public void setSignalRecordingsList(ArrayList<Signal> signalRecordingsList) {
        this.signalRecordingsList = signalRecordingsList;
    }

    public void addReport(Signal recording) {
        this.signalRecordingsList.add(recording);
    }

    public ArrayList<Report> getSymptomsList() {
        return symptomsList;
    }

    public void setSymptomsList(ArrayList<Report> symptomsList) {
        this.symptomsList = symptomsList;
    }
    public void addSymptom(Report symptom) {

        this.symptomsList.add(symptom);
        System.out.println("Symptom added: " + symptom);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(int doctor_id) {
        this.doctor_id = doctor_id;
    }

    /**
     * Creates a new {@code Patient} instance from a {@link JsonObject}.
     *
     * @param json  the JSON object containing this {@code Patient} data
     * @return  a {@code Patient} instance from the {@link JsonObject}
     *
     * @see JsonObject
     */
    public static Patient fromJason(JsonObject json) {
        Patient patient = new Patient();
        patient.setId(json.get("id").getAsInt());
        patient.setName(json.get("name").getAsString());
        patient.setSurname(json.get("surname").getAsString());
        patient.setEmail(json.get("email").getAsString());
        patient.setPhone(json.get("contact").getAsInt());
        patient.setDateOfBirth(LocalDate.parse(json.get("dateOfBirth").getAsString()));
        patient.setGender(json.get("gender").getAsString());
        patient.setDoctor_id(json.get("doctorId").getAsInt());


        // ----- SIGNALS -----
        if (json.has("signals")) {
            JsonArray signalsJson = json.getAsJsonArray("signals");
            ArrayList<Signal> signals = new ArrayList<>();

            for (JsonElement elem : signalsJson) {
                JsonObject sJson = elem.getAsJsonObject();
                signals.add(Signal.fromJson(sJson));
            }
            patient.setSignalRecordingsList(signals);
        }

        // ----- SYMPTOMS / REPORTS -----
        if (json.has("reports")) {
            JsonArray symptomsJson = json.getAsJsonArray("reports");
            ArrayList<Report> reports = new ArrayList<>();

            for (JsonElement elem : symptomsJson) {
                JsonObject rJson = elem.getAsJsonObject();
                reports.add(Report.fromJson(rJson));
            }
            patient.setSymptomsList(reports);
        }
        return patient;
    }

    /**
     * Converts this {@code Patient} into a {@link JsonObject}. The JSON object specifies all public fields
     * except the {@code active} field //TODO: por que no lo especifica?
     *
     * @return  a JSON representation of this patient
     *
     * @see JsonObject
     */
    public JsonObject toJason() {
        JsonObject jason = new JsonObject();
        jason.addProperty("id", id);
        jason.addProperty("name", name);
        jason.addProperty("surname", surname);
        jason.addProperty("email", email);
        jason.addProperty("contact", phone);
        jason.addProperty("dateOfBirth", dateOfBirth.toString());
        jason.addProperty("gender", gender);
        jason.addProperty("doctorId", doctor_id);
        return jason;
    }

    public Object toJson() {
        JsonObject jason = new JsonObject();
        jason.addProperty("id", id);
        jason.addProperty("name", name);
        jason.addProperty("surname", surname);
        jason.addProperty("email", email);
        jason.addProperty("contact", phone);
        jason.addProperty("dateOfBirth", dateOfBirth.toString());
        jason.addProperty("gender", gender);
        jason.addProperty("doctorId", doctor_id);
        return jason;
    }
}
