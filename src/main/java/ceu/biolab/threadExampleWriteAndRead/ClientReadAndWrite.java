/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ceu.biolab.threadExampleWriteAndRead;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sir_D
 */
public class ClientReadAndWrite {

    public static void main(String args[]) throws IOException {
        InputStream in = System.in;
        PrintStream out = System.out;
        new Thread(new Read(in, out)).start();
        new Thread(new Write(in, out)).start();
        System.out.println("Main Thread Finish here!!!!");
    }
}

class Write implements Runnable {
    InputStream in;
    PrintStream out;
    Write(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }
    @Override
    public void run() {
        int counter = 0;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
            }
            counter++;
            out.println("Time elapsed:" + counter);
        }
    }
}

class Read implements Runnable {
    InputStream in;
    PrintStream out;
    Read(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }
    @Override
    public void run() {
        int byteRead;
        while (true) {
            try {
                //Read from our console
                byteRead = in.read();
                //And send it to the server
                out.println("I have read:" + byteRead);
            } catch (IOException ex) {
                Logger.getLogger(Read.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
