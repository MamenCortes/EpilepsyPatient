package model;

import java.util.ArrayList;

public class Patient {

    private String name;
    private String surname;
    private String email;
    private Integer phone;
    private String sex;
    private String dateOfBirth;
    private ArrayList<Report> reports;
    private ArrayList<Signal> signals;

    public Patient(String name, String surname, String email, Integer phone, String sex, String dateOfBirth) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.reports = new ArrayList<Report>();
        this.signals = new ArrayList<Signal>();
    }

    public Patient() {
        this.name = "";
        this.surname = "";
        this.email = "";
        this.phone = 123456789;
        this.sex = "NonBinary";
        this.dateOfBirth = "01-01-1999";
        this.reports = new ArrayList<Report>();
        this.signals = new ArrayList<Signal>();
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
                ", numReports='" + reports.size() + '\'' +
                ", numSignals='" + signals.size() + '\'' +
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

    public ArrayList<Report> getReports() {
        return reports;
    }

    public void setReports(ArrayList<Report> reports) {
        this.reports = reports;
    }

    public ArrayList<Signal> getSignals() {
        return signals;
    }

    public void setSignals(ArrayList<Signal> signals) {
        this.signals = signals;
    }

    public void addReport(Report report) {
        this.reports.add(report);
    }

    public void addSignal(Signal signal) {
        this.signals.add(signal);
    }
}
