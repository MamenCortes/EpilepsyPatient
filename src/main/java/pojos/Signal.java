package pojos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ui.SignalRecorderService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Signal {
    private String comments;
    private int samplingFrequency;
    private String timeStamp;
    private File rawSignal;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    public Signal(String date, int samplingFrequency, File rawSignal, String timeStamp) {
        this.samplingFrequency = samplingFrequency;
        this.rawSignal = rawSignal;
        this.timeStamp = timeStamp;
        this.comments = "";
        this.date = date;
    }

    public Signal() {
        super();
        this.samplingFrequency = 100;
        this.rawSignal = null;
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

   public File getRawSignal() {
        return rawSignal;
    }
    public void setRawSignal(File rawSignal) {
        this.rawSignal = rawSignal;
   }

    public static class SignalMetadataDTO {
        public Integer patientId;
        public String comments;
        public int samplingFrequency;
        public String timestamp;  // en ISO string
        public String zipFileName;
    }
    public String buildSignalMetadataJson(Signal signal,int patientId) {
        SignalMetadataDTO dto = new SignalMetadataDTO();
        dto.patientId = patientId;
        dto.comments = signal.getComments();
        dto.samplingFrequency = signal.getSamplingFrequency();
        dto.timestamp = signal.getTimeStamp().toString();
        dto.zipFileName = signal.getRawSignal().getName();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(dto);
    }
}
