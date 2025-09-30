package ceu.biolab.threadsCreation;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TravelerRunnable implements Runnable {

    public TravelerRunnable(Integer number) {
        this.number = number;
    }

    private Integer number;

    @Override
    public void run() {
        Integer wait = new Double(Math.random() * 10000).intValue();
        try {
            Thread.sleep(wait);
        } catch (InterruptedException ex) {
            Logger.getLogger(TravelerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("I am Traveler " + number + " saying hello usign Runnable");
        }
    }

}
