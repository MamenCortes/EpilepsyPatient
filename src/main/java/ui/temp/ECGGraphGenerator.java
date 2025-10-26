package ui.temp;
import org.jfree.chart.ChartFactory; //utilities to create charts
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart; //utilities to create charts
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Millisecond; //a RegularTimePeriod representing millisecond resolution; used for time-stamped points.
import org.jfree.data.time.TimeSeries; //holds a sequence of (time, value) pairs where time is represented by JFreeChart’s RegularTimePeriod subclasses (e.g. Millisecond).
import org.jfree.data.time.TimeSeriesCollection; //a dataset container that can hold one or more TimeSeries objects; passed to chart factories.
import ui.windows.Application;
import ui.components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

public class ECGGraphGenerator extends JFrame {  // Extends ApplicationFrame for displaying the chart

    public ECGGraphGenerator(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void plotECG(int samplingFrequency, String startTime, double duration, double[] amplitudeValues) {
        try {
            // Step 1: Parse the start time
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date startDateTime = dateFormat.parse(startTime);  // Assuming startTime is like "00:00:00"

            //TimeSeries holds ordered (timePeriod, value) pairs.
            // Each point uses a RegularTimePeriod (here Millisecond), and the class expects increasing time keys
            TimeSeries series = new TimeSeries("ECG Recording");

            // Step 2: Add data points to the TimeSeries
            //double timeIntervalSeconds = 1.0 / samplingFrequency;  // Time between samples in seconds
            //long millisecondInterval = (long) (timeIntervalSeconds * 1000);  // Convert to milliseconds

            // compute startMillis using java.time
            LocalTime t = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
            LocalDateTime start = LocalDate.now().atTime(t);
            long startMillis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            for (int i = 0; i < amplitudeValues.length; i++) {
                //long currentTimeMillis = startDateTime.getTime() + (i * millisecondInterval);
                long currentTimeMillis = startMillis + Math.round(i * (1000.0 / samplingFrequency));
                Millisecond timePoint = new Millisecond(new Date(currentTimeMillis));
                //Store the point for plotting
                series.add(timePoint, amplitudeValues[i]);
            }

            // Step 3: Create the dataset and chart
            //TimeSeriesCollection is the dataset type that createTimeSeriesChart expects:
            // it can contain multiple TimeSeries.
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);
            //ChartFactory.createTimeSeriesChart(...) returns a JFreeChart configured as a time series plot:
            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    "ECG Recording", //title
                    "Time", //x axis
                    "Amplitude",//y axis
                    dataset, //values
                    true,  // Legend
                    true,  // Tooltips
                    false  // URLs
            );

            //Change aesthetics of the chart
            XYPlot plot = chart.getXYPlot();
            // Background color of the plot area (inside axes)
            plot.setBackgroundPaint(Color.white); // light blue/grey
            // Chart border and overall background
            chart.setBackgroundPaint(Color.WHITE);
            // Gridlines
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            // Change the color and stroke of the ECG line
            XYItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Application.darker_turquoise);  // purple ECG line
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));   // thicker line

            // Step 4: Display the chart
            //ChartPanel is a Swing component which renders the chart and supports zooming/panning and tooltips.
            ChartPanel chartPanel = new ChartPanel(chart);
            /*ECGGraphGenerator frame = new ECGGraphGenerator("ECG Chart");
            frame.setContentPane(chartPanel);
            frame.pack();
            frame.setVisible(true);*/

            JButton resetZoom = new MyButton("Reset Zoom");
            resetZoom.addActionListener(e -> chartPanel.restoreAutoBounds());

            JFrame frame = new JFrame("ECG Chart");
            frame.setLayout(new BorderLayout());
            frame.add(chartPanel, BorderLayout.CENTER);
            frame.add(resetZoom, BorderLayout.SOUTH);
            frame.pack();
            frame.setVisible(true);

        } catch (ParseException e) {
            System.err.println("Error parsing start time: " + e.getMessage());
        }
    }

    // Example method to generate random ECG data for testing
    public static double[] generateRandomECGData(int samplingFrequency, double duration) {
        int numSamples = (int) (samplingFrequency * duration);
        double[] data = new double[numSamples];
        Random random = new Random();
        for (int i = 0; i < numSamples; i++) {
            data[i] = 0 + (random.nextDouble() * 2 - 1);  // Random values between -1 and 1 for simulation
        }
        return data;
    }

    // Main method for testing
    public static void main(String[] args) {
        int samplingFrequency = 100;  // 100 samples per second
        String startTime = "00:00:00";  // Start time
        double duration = 10.0;  // 10 seconds
        double[] amplitudeValues = generateRandomECGData(samplingFrequency, (int) duration);  // Generate random data

        plotECG(samplingFrequency, startTime, duration, amplitudeValues);
    }
}
