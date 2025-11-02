package pojos;

import com.google.gson.JsonObject;

public class Doctor {

    private Integer id;
    private String name;
    private String surname;
    private String email;
    private Integer phone;
    private String department;
    private String speciality;

    public Doctor(String name, String surname, String email, Integer phone, String department) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.speciality = department;
        this.id = 0;
    }

    public Doctor() {
        this.name = "";
        this.surname = "";
        this.email = "";
        this.phone = 123456789;
        this.department = "";
        this.speciality = "";
        this.id = 0;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                ", address='" + department + '\'' +
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

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public static Doctor fromJason(JsonObject json) {
        Doctor doctor = new Doctor();
        doctor.setId(json.get("id").getAsInt());
        doctor.setName(json.get("name").getAsString());
        doctor.setSurname(json.get("surname").getAsString());
        doctor.setPhone(json.get("contact").getAsInt());
        doctor.setEmail(json.get("email").getAsString());
        doctor.setDepartment(json.get("department").getAsString());
        doctor.setSpeciality(json.get("speciality").getAsString());
        return doctor;
    }
}
