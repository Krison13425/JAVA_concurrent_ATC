package Assignment;

import static Assignment.Main.minutesToMilliseconds;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Maxine
 */
public class Passenger implements Runnable {

    public int id;
    public Airplane plane;
    public AtomicBoolean Disembarked;
    public AtomicBoolean Embarked;

    public Passenger(Airplane plane, char status) {
        plane.getAirport().passengerCount++;
        this.id = plane.getAirport().passengerCount;
        this.plane = plane;
        if (status == 'L') {
            //This passsenger is going to disembark from plane.
            Disembarked = new AtomicBoolean(false);
            Embarked = new AtomicBoolean(true);
        } else if (status == 'T') {
            //This passsenger is going to embark to plane.
            Disembarked = new AtomicBoolean(true);
            Embarked = new AtomicBoolean(false);
        }
        
    }

    @Override
    public void run() {
        synchronized (this) {
            while (!Disembarked.get()) {

                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    System.err.println("Problem occurs when passsenger " + id + " is waiting to be embark to " + plane.getAirplaneID());
                }

                disembark();
            }
            while (!Embarked.get()) {

                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    System.err.println("Problem occurs when passsenger " + id + " is waiting to be embark to " + plane.getAirplaneID());
                }

                embark();
            }
        }

    }

    public synchronized void disembark() {
        try {
            //Two passengers are allowed to disembark at the same time
            plane.getGate().acquire();
            Thread.sleep(new Random().nextInt(60) * minutesToMilliseconds / 60);
            System.out.println("Passenger " + id + " disembarked from " + plane.getAirplaneID());
            plane.getGate().release();
            plane.getPassengersOnBoard().take();
            Disembarked.set(true);
            plane.getSystemStatistic().DisembarkedPassenger();
        } catch (InterruptedException ex) {
            plane.getGate().release();
            System.err.println("Problem occurs when passsenger " + id + " is disembarking from " + plane.getAirplaneID());
        }

    }

    public synchronized void embark() {
        try {
            //Two passengers are allowed to disembark at the same time
            plane.getGate().acquire();
            Thread.sleep(new Random().nextInt(60) * minutesToMilliseconds / 60);
            System.out.println("Passenger " + id + " embarked to " + plane.getAirplaneID());

            if (plane.getPassengersOnBoard().offer(this)) {
               Embarked.set(true);
                plane.getSystemStatistic().EmbarkedPassenger();
            } 
            plane.getGate().release();

        } catch (InterruptedException ex) {
            plane.getGate().release();
            System.err.println("Problem occurs when passsenger " + id + " is embarking to " + plane.getAirplaneID());
        }

    }

}
