package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public abstract class Report {
    private String date;  // The unique attribute 'date'

    public Report() {
        date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
    }
    public Report(String date) {
        this.date = date;
    }
    // Getter for date
    public String getDate() {
        return date;
    }

    // Setter for date
    public void setDate(String date) {
        this.date = date;
    }
}
