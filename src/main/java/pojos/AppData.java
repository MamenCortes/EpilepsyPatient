package pojos;

public class AppData {
    Patient patient;
    User user;
    public AppData(Patient patient,User user) {
        this.patient = patient;
        this.user = user;
    }
    public AppData(){
    }
    public Patient getPatient() {
        return patient;
    }
    public void setPatient(Patient patient) {this.patient = patient;}
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    @Override
    public String toString() {
        return  "AppData{" + "patient=" + patient + ", user=" + user + '}';
    }
}

