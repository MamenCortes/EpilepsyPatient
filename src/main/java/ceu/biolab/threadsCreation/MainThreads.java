/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ceu.biolab.threadsCreation;

/**
 *
 * @author Sir_D
 */
public class MainThreads {

    public static void main(String args[]) {
        Integer limit = 100;
        System.out.println("Creating " + limit + " travellers");
        for (int i = 0; i < limit; i++) {
            TravelerThread travelerThread = new TravelerThread(i);
            travelerThread.start();
        }
        System.out.println("Main Thread Finish");
    }

}
