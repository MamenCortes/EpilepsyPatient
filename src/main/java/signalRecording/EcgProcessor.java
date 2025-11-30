package signalRecording;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
/**
 * Processes streaming ECG samples in real-time using a sliding 8-second window.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Maintains a time-based window of recent ECG samples</li>
 *     <li>Tracks timestamps for RR-interval computation</li>
 *     <li>Detects R-peaks using an adaptive threshold</li>
 *     <li>Computes heart rate (BPM)</li>
 *     <li>Estimates if heart rate is rising compared to a short-term baseline</li>
 * </ul>
 * This is a lightweight real-time approximation suitable for wearables
 * or BITalino-style ECG streams.
 */
public class EcgProcessor {
    private final int WINDOW_MS = 8000; //8 seconds of recent ECG data
    private final Deque<Double> ecgWindow = new ArrayDeque<>(); //stores raw ECG samples
    private final Deque<Long> timeWindow = new ArrayDeque<>(); //stores the timestamp of each sample
    //stores the timestamp of the peak
    private final List<Long> rPeaks = new ArrayList<>();

    // Simple adaptive threshold
    //Detects heartbeats by looking for samples larger than a threashold
    private double threshold = 500;

    /**
     * Adds a new ECG sample to the processor and updates the sliding 8-second window.
     * Performs simple adaptive-threshold R-peak detection.
     *
     * <p>Steps:</p>
     * <ol>
     *     <li>Store the sample and its timestamp</li>
     *     <li>Remove samples older than 8 seconds</li>
     *     <li>If amplitude exceeds adaptive threshold → detect R-peak</li>
     *     <li>Adapt threshold upward for peak, downward for noise</li>
     *     <li>Keep at most 20 recent R-peaks</li>
     * </ol>
     *
     * @param ecg The ECG sample amplitude
     * @param ts  Timestamp of the sample (in milliseconds)
     */
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
    /**
     * Computes the current heart rate in beats per minute using recent RR intervals.
     *
     * @return Heart rate in BPM, or {@code Double.NaN} if insufficient data.
     */
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
    /**
     * Determines whether the current heart rate is rising significantly
     * compared to earlier RR-interval-derived baseline.
     *
     * @return {@code true} if the heart rate has increased by at least 15 BPM,
     *         {@code false} otherwise.
     */

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
/**
 * Represents the estimated movement state derived from accelerometer variance.
 * <ul>
 *     <li>{@link #CALM} – minimal movement</li>
 *     <li>{@link #MODERATE} – medium movement (walking, jostling)</li>
 *     <li>{@link #ABNORMAL} – intense or irregular movement, possibly pathological</li>
 * </ul>
 */

enum MovementState { CALM, MODERATE, ABNORMAL }
/**
 * Processes accelerometer magnitude samples using a fixed-size sliding window.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Maintains the most recent 128 accelerometer magnitude samples</li>
 *     <li>Computes mean and variance of movement</li>
 *     <li>Classifies movement intensity into CALM, MODERATE, or ABNORMAL</li>
 * </ul>
 * Variance thresholds are approximate and should be tuned for real signals.
 */

class AccProcessor {
    private final int WINDOW = 128;
    private final List<Double> mags = new ArrayList<>();
    /**
     * Adds one accelerometer magnitude sample to the sliding window.
     *
     * @param acc Accelerometer magnitude (e.g., sqrt(x² + y² + z²))
     * @param ts  Timestamp of the sample (not used directly)
     */
    public void addSample(double acc, long ts) {
        //calculates the avg
        mags.add(acc);
        if (mags.size() > WINDOW) mags.remove(0);
    }
    /**
     * Computes the current movement state based on the variance
     * of recent accelerometer magnitude samples.
     *
     * <p>Thresholds (tunable):</p>
     * <ul>
     *     <li>variance &gt; 30000 → ABNORMAL</li>
     *     <li>variance &gt; 8000 → MODERATE</li>
     *     <li>otherwise → CALM</li>
     * </ul>
     *
     * @return The estimated {@link MovementState}.
     */
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

