package model;

import java.util.ArrayList;

public class Patient {

    private String name;
    private String surname;
    private String email;
    private Integer phone;
    private String sex;
    private String dateOfBirth;
    private ArrayList<SignalRecording> signalRecordingsList;
    private ArrayList<SymptomReport> symptomsList;



    public Patient(String name, String surname, String email, Integer phone, String sex, String dateOfBirth) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.signalRecordingsList = new ArrayList<SignalRecording>();
        this.symptomsList = new ArrayList<SymptomReport>();
    }

    public Patient() {
        this.name = "";
        this.surname = "";
        this.email = "";
        this.phone = 123456789;
        this.sex = "NonBinary";
        this.dateOfBirth = "01-01-1999";
        this.signalRecordingsList = new ArrayList<SignalRecording>();
        this.symptomsList = new ArrayList<SymptomReport>();
    }

    @Override
    public String toString() {
        return "Patient{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                ", sex='" + sex + '\'' +
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public ArrayList<SignalRecording> getSignalRecordingsList() {
        return signalRecordingsList;
    }

    public void setSignalRecordingsList(ArrayList<SignalRecording> signalRecordingsList) {
        this.signalRecordingsList = signalRecordingsList;
    }

    public void addReport(SignalRecording recording) {
        this.signalRecordingsList.add(recording);
    }

    public ArrayList<SymptomReport> getSymptomsList() {
        return symptomsList;
    }

    public void setSymptomsList(ArrayList<SymptomReport> symptomsList) {
        this.symptomsList = symptomsList;
    }
    public void addSymptom(SymptomReport symptom) {
        this.symptomsList.add(symptom);
    }

}
