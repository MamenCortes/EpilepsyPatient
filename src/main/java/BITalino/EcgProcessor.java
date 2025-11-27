package BITalino;

import Events.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class EcgProcessor {
    private final int WINDOW_MS = 8000; //8 seconds of recent ECG data
    private final Deque<Double> ecgWindow = new ArrayDeque<>(); //stores raw ECG samples
    private final Deque<Long> timeWindow = new ArrayDeque<>(); //stores the timestamp of each sample
    //stores the timestamp of the peak
    private final List<Long> rPeaks = new ArrayList<>();

    // Simple adaptive threshold
    //Detects heartbeats by looking for samples larger than a threashold
    private double threshold = 500;

    ///Each time you store the sample, you store the value and its timestamp. You remove samples older than 8 seconds.
    /// This ensures processing always uses a sliding window of recent ECG
    public void addSample(double ecg, long ts) {
        ecgWindow.add(ecg);
        timeWindow.add(ts);

        while (timeWindow.peek() < ts - WINDOW_MS) {
            ecgWindow.poll();
            timeWindow.poll();
        }

        // Simple R-peak detection: local peak crosses threshold
        //Good as an approximation
        //It's essential for the threashold to be dynamic because ECG amplitude varies between people, electrode placement, BITalino gain settings, etc.
        if (ecg > threshold) {
            //threshold is increased slightly to avoid falsely detecting noise
            threshold = threshold * 0.9 + ecg * 0.1; // adapt to noise/ECG amplitude
            rPeaks.add(ts);
        } else {
            threshold *= 0.999; // slow decay
            //If not, threshold slowly decays so the detector can become sensitive again
        }

        // keep only last few peaks
        while (rPeaks.size() > 20) rPeaks.remove(0);
    }

    public double getCurrentHeartRate() {
        if (rPeaks.size() < 2) return Double.NaN;

        long sum = 0;
        for (int i = 1; i < rPeaks.size(); i++) {
            sum += (rPeaks.get(i) - rPeaks.get(i - 1));
            //stores the difference between consecutive peaks
        }
        //makes an average of the 8 seconds considered in the window
        double avgRR = sum / (double) (rPeaks.size() - 1);

        return 60000.0 / avgRR; //convert to beats per minute
    }

    public boolean isHeartRateRising() {
        if (rPeaks.size() < 5) return false;

        // Compare last 2 RR intervals to earlier ones
        double hrNow = getCurrentHeartRate();
        if (Double.isNaN(hrNow)) return false;

        double hrBaseline = 0;
        int count = 0;

        for (int i = 1; i < rPeaks.size() - 3; i++) {
            long diff = rPeaks.get(i) - rPeaks.get(i - 1);
            hrBaseline += 60000.0 / diff;
            count++;
        }

        hrBaseline /= Math.max(1, count);

        return hrNow > hrBaseline + 15; // rising by +15 bpm
    }
}

enum MovementState { CALM, MODERATE, ABNORMAL }

class AccProcessor {
    private final int WINDOW = 128;
    private final List<Double> mags = new ArrayList<>();

    public void addSample(double ax, double ay, double az, long ts) {
        double mag = Math.sqrt(ax*ax + ay*ay + az*az); //calculates the avg
        mags.add(mag);
        if (mags.size() > WINDOW) mags.remove(0);
    }

    public MovementState getMovementState() {
        if (mags.size() < WINDOW) return MovementState.CALM;

        double mean = mags.stream().mapToDouble(a -> a).average().orElse(0);
        double var = mags.stream().mapToDouble(a -> (a - mean) * (a - mean)).sum() / mags.size();

        //TODO: refine thresholds
        if (var > 30000) return MovementState.ABNORMAL;
        if (var > 8000) return MovementState.MODERATE;
        return MovementState.CALM;
    }
}

