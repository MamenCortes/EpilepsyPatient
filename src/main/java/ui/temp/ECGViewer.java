package ui.temp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import ui.components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Date;

public class ECGViewer extends JFrame {
    private TimeSeries ecgSeries;
    private int windowSize = 10000;  // show 10,000 samples (~10s at 1kHz)
    private int currentIndex = 0;
    private double[] fullData;

    public ECGViewer(double[] fullData, int samplingRate) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.fullData = fullData;

        ecgSeries = new TimeSeries("ECG");
        TimeSeriesCollection dataset = new TimeSeriesCollection(ecgSeries);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "ECG Signal", "Time", "Amplitude", dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel);

        //Change aesthetics and axis limits
        XYPlot plot = chart.getXYPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); // eje Y
        rangeAxis.setAutoRange(false); // desactiva que el eje se ajuste automáticamente
        rangeAxis.setRange(-1, 1);


        // initial window
        updateWindow(0, windowSize);

        // add buttons to scroll left/right
        JButton left = new MyButton("←");
        JButton right = new MyButton("→");
        JButton resetZoom = new MyButton("Reset Zoom");
        JPanel nav = new JPanel();
        nav.add(left);
        nav.add(right);
        nav.add(resetZoom);
        add(nav, BorderLayout.SOUTH);

        left.addActionListener(e -> scroll(-windowSize / 2));
        right.addActionListener(e -> scroll(windowSize / 2));
        //If autorange
        //resetZoom.addActionListener(e -> chartPanel.restoreAutoBounds());
        //IF fixed y bounds to -1,1
        resetZoom.addActionListener(e -> {
            // Get the plot
            XYPlot plot2 = chart.getXYPlot();
            // Set the y-axis range to [-1, 1]
            plot2.getRangeAxis().setRange(-1.0, 1.0);
            // Optionally reset the x-axis to auto or some fixed range
            plot.getDomainAxis().setAutoRange(true);
        });

        pack();
        setVisible(true);
    }

    private void updateWindow(int start, int end) {
        ecgSeries.clear();
        for (int i = start; i < end && i < fullData.length; i++) {
            ecgSeries.addOrUpdate(new Millisecond(new Date(i)), fullData[i]);
        }
    }

    private void scroll(int delta) {
        currentIndex = Math.max(0, Math.min(currentIndex + delta, fullData.length - windowSize));
        updateWindow(currentIndex, currentIndex + windowSize);
    }

    /**
     * Centers and normalizes a signal.
     * @param signal Original signal values
     * @return Processed signal (centered around 0, normalized between -1 and 1)
     */
    public static double[] preprocessSignal(double[] signal) {
        int n = signal.length;
        double[] processed = new double[n];

        // Step 1: Compute the mean (centering)
        double sum = 0;
        for (double v : signal) sum += v;
        double mean = sum / n;

        // Step 2: Center the signal
        double maxAbs = 0; // to find max absolute value for normalization
        for (int i = 0; i < n; i++) {
            processed[i] = signal[i] - mean;
            if (Math.abs(processed[i]) > maxAbs) maxAbs = Math.abs(processed[i]);
        }

        // Step 3: Normalize to [-1, 1]
        if (maxAbs > 0) {
            for (int i = 0; i < n; i++) {
                processed[i] /= maxAbs;
            }
        }

        return processed;
    }

    /**
     * Returns a subarray starting after the first minute of recording.
     * @param signal Original signal
     * @param samplingFrequency Frecuencia de muestreo en Hz
     * @return Subarray de la señal a partir del minuto 1
     */
    public static double[] skipFirstMinute(double[] signal, int samplingFrequency) {
        int samplesToSkip = samplingFrequency * 60; // 60 segundos * frecuencia de muestreo
        if (samplesToSkip >= signal.length) {
            // Si la señal dura menos de un minuto, devolvemos un array vacío
            return new double[0];
        }

        double[] subSignal = new double[signal.length - samplesToSkip];
        System.arraycopy(signal, samplesToSkip, subSignal, 0, subSignal.length);
        return subSignal;
    }

    public static void main(String[] args) {
        String filePath = "C:/path/to/record.txt";
        filePath = "C:\\Users\\mamen\\OneDrive - Fundación Universitaria San Pablo CEU\\06_BecaPregrado\\2023-2024\\Prototipo_registros\\Prototipo_Day1_2024-02-01_11-20-02.txt";
        try {
            String date = "2024-02-01";
            int samplingFrequency = 1000; // Adjusted sampling rate = 10 Hz
            String startTime = "11:20:02";

            double[] ecg = ECGFileReader.readECGFromFile(filePath);
            double[] trimmedECG = skipFirstMinute(ecg, samplingFrequency);
            System.out.println("Loaded " + ecg.length + " samples.");

            //Process the signal: center and normalize
            double[] processedSignal = preprocessSignal(trimmedECG);

            // example data
            //double[] data = new double[100000];
            //for (int i = 0; i < data.length; i++) data[i] = Math.sin(i / 100.0);
            new ECGViewer(processedSignal, samplingFrequency);
        }catch (IOException e){
            System.out.println("Error reading file");
        }
    }
}
