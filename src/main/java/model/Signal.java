package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Signal extends Report{
    private String comments;
    private int samplingFrequency;
    private String ECG;
    private String ACC;
    private String timeStamp;

    public Signal(String symptoms, String date, int samplingFrequency, String ECG, String ACC, String timeStamp) {
        super(date);
        this.samplingFrequency = samplingFrequency;
        this.ECG = ECG;
        this.ACC = ACC;
        this.timeStamp = timeStamp;
        this.comments = "";
    }

    public Signal() {
        super();
        this.samplingFrequency = 100;
        this.ECG = "";
        this.ACC = "";
        this.timeStamp = "";
        this.comments = "";
    }

    @Override
    public String toString() {
        return "Signal{" +
                "date='" + getDate() + '\'' +  // Using inherited getter
                ", comments='" + comments + '\'' +
                ", samplingFrequency=" + samplingFrequency +
                '}';
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

    public String getACC() {
        return ACC;
    }

    public void setACC(String ACC) {
        this.ACC = ACC;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
