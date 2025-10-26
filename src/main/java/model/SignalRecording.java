package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SignalRecording {
    private String comments;
    private int samplingFrequency;
    private String ECG;
    private String ACCx;
    private String ACCy;
    private String ACCz;
    private String timeStamp;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    public SignalRecording(String date, int samplingFrequency, String ECG, String ACCx, String ACCy, String ACCz, String timeStamp) {
        this.samplingFrequency = samplingFrequency;
        this.ECG = ECG;
        this.ACCx = ACCx;
        this.ACCy = ACCy;
        this.ACCz = ACCz;
        this.timeStamp = timeStamp;
        this.comments = "";
        this.date = date;
    }

    public SignalRecording() {
        super();
        this.samplingFrequency = 100;
        this.ECG = "";
        this.ACCx = "";
        this.ACCy = "";
        this.ACCz = "";
        this.timeStamp = "";
        this.comments = "";
        this.date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "date='" + getDate() + '\'' +  // Using inherited getter
                ", comments='" + comments;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getSamplingFrequency() {
        return samplingFrequency;
    }

    public void setSamplingFrequency(int samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
    }

    public String getECG() {
        return ECG;
    }

    public void setECG(String ECG) {
        this.ECG = ECG;
    }

    public String getACCx() {
        return ACCx;
    }

    public void setACCx(String ACCx) {
        this.ACCx = ACCx;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getACCy() {
        return ACCy;
    }

    public void setACCy(String ACCy) {
        this.ACCy = ACCy;
    }

    public String getACCz() {
        return ACCz;
    }

    public void setACCz(String ACCz) {
        this.ACCz = ACCz;
    }
}
