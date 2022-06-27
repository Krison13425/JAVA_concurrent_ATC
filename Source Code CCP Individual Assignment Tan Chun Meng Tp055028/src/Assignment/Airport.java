package Assignment;

import static Assignment.Main.minutesToMilliseconds;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Hashtable;


public class Airport {

    private volatile AtomicBoolean isRunning;
    private Thread[] gateThreads;
    private Thread[] airplaneThreads;
    private Airplane[] airplane;

    public static int passengerCount = 0;
    private BlockingQueue<Airplane> Queue;
    private ReentrantLock runway;
    private ReentrantLock dockinglane;
    private LaneControl lc;
    int airplaneCount = 0;
    int passengerAmount = 0;
    private Hashtable<String, LinkedList<Passenger>> waitinglist;
    
    class CompareOrder<T extends Airplane> implements Comparator<T> {

        @Override
        public int compare(T left, T right) {
            if (left.getStatus().get() == Status.LANDING && right.getStatus().get() != Status.LANDING) { //push left the queue up
                return -1;
            } else if (right.getStatus().get() == Status.LANDING && left.getStatus().get() != Status.LANDING) {//push left queue down
                return 1;  //push left false down the queue
            } else {
                return 0;
            }
        }

    }
    
    public Airport(int gateAmount, int roadLength, int standbyLength, int Queueline) {
        lc = new LaneControl();
        gateThreads = new Thread[gateAmount];

        lc.setGateToDockinglane(new Semaphore[gateAmount]);
        char gateName = 'A';
        for (int i = 0; i < gateAmount; i++, gateName++) {
            gateThreads[i] = new Thread(new TerminalGate(gateName, this));
            if (i != gateAmount - 1) {
                lc.setGateAToDockinglane(i, new Semaphore(roadLength, true));
            }
        }
        
        airplaneThreads = new Thread[6];
        airplane = new Airplane[6];
        isRunning = new AtomicBoolean(true);
        // blocking queue on airplane
        Queue = new LinkedBlockingQueue<>(Queueline);
        //locking the airport single lane
        runway = new ReentrantLock(true);
        dockinglane = new ReentrantLock(true); 
        // semaphore to control the airplane lane
        lc.setRunwayToGate(new Semaphore(roadLength, true));
        lc.setDockinglaneToGate(new Semaphore(roadLength, true));
        lc.setRunwayToDockinglane(new Semaphore(roadLength, true));
        lc.setDockinglaneToTakeofflane(new Semaphore(roadLength, true));
        lc.setGateToTakeofflane(new Semaphore(roadLength, true));
        lc.setReadyTakeoff(new Semaphore(standbyLength, true));
        
        waitinglist = new Hashtable<String, LinkedList<Passenger>>();
    }

    
    public void run() {
    	// enable gate
        for (Thread g : gateThreads) {
            g.start();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Airport.class.getName()).log(Level.SEVERE, null, ex);
        }

        Thread airplaneGenerator = new Thread() {
            @Override
            public void run() {
                while (airplaneCount < 6) {
                    airplaneCount++;
                    Airplane newPlane = new Airplane("" + airplaneCount, Airport.this);
                    
                    LinkedList<Passenger> passenger = new LinkedList();
                    
                    int passengerAmount = new Random().nextInt(50) + 1;
                    
                    for (int i = 0; i < passengerAmount; i++) 
                    {
                        Passenger boardingPassenger = new Passenger(newPlane, 'T');
                        passenger.add(boardingPassenger);
                        Thread PassengerThread = new Thread(boardingPassenger);
                        PassengerThread.start();
                    }
                    
                    getWaitingList().put("" + airplaneCount, passenger);
                    
                    airplane[airplaneCount - 1] = newPlane;
                    airplaneThreads[airplaneCount - 1] = new Thread(newPlane);
                    airplaneThreads[airplaneCount - 1].start();

                    try {
                        Thread.sleep(new Random().nextInt(4) * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        airplaneGenerator.start();

        try {
        	//check threads have done execution
            airplaneGenerator.join();
            int counter = 0;
            for (Thread airplane : airplaneThreads) {
                counter++;
                airplane.join();
                System.out.println("Plane " + counter + " has done execution");
            }
            isRunning.set(false);
            counter = 0;
            for (Thread g : gateThreads) {
                counter++;
                g.join();
                System.out.println("Gate " + counter + " has done execution");
            }

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        printStatistics();
    }
    
    public void printStatistics() {
        int totalAirplaneServed = 0;
        int totalPassengers = 0;
        int totalPassengersEmbarked = 0;
        int totalPassengersDisembarked = 0;
        long maxwaitingTime = 0;
        long minwaitingTime = 0;
        long avgwaitingTime = 0;
        
        try {
            Thread.sleep(1000);

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        for (Airplane airplane : airplane) {
            if (airplane.getSystemStatistic().Doneexecute()) {
                totalAirplaneServed++;

                totalPassengersEmbarked += airplane.getSystemStatistic().getEmbarkedPassenger().get();
                totalPassengersDisembarked += airplane.getSystemStatistic().getDisembarkedPassenger().get();
                
                long waitingTime = airplane.getSystemStatistic().getTakeoffTime() - airplane.getSystemStatistic().getArrivingTime();
                
                if (waitingTime > maxwaitingTime) {
                	
                    maxwaitingTime = waitingTime;
                }
                if (totalAirplaneServed == 1) 
                {
                    minwaitingTime = waitingTime;
                } 
                else if (waitingTime <= minwaitingTime) 
                {
                    minwaitingTime = waitingTime;
                }
                
                avgwaitingTime =  (avgwaitingTime * (totalAirplaneServed - 1) + (waitingTime)) / totalAirplaneServed;
            }
        }
        totalPassengers = totalPassengersEmbarked + totalPassengersDisembarked;
        
        
        System.out.println("/n Total Airplanes Served: " + totalAirplaneServed);
        System.out.println("Total Passengers: " + totalPassengers);
        System.out.println("Total Passengers Embarked: " + totalPassengersEmbarked);
        System.out.println("Total Passengers Disembarked: " + totalPassengersDisembarked);
        System.out.println("Maximum Waiting Time: " + maxwaitingTime);
        System.out.println("Minimum Turnaround Time: " + minwaitingTime);
        System.out.println("Average Turnaround Time: " + avgwaitingTime);
    }

    
    public int PassengerAmount (String id) {
        LinkedList flightWaitingPassengers = waitinglist.get(id);
        if (flightWaitingPassengers == null) {
            return 0;
        } else {
            return flightWaitingPassengers.size();
        }
    }
    
    public Hashtable<String, LinkedList<Passenger>> getWaitingList() {
        return waitinglist;
    }
    
    public LinkedList<Passenger> getWaitingList(String id) {
        return waitinglist.get(id);
    }
    
    public AtomicBoolean getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(AtomicBoolean isRunning) {
        this.isRunning = isRunning;
    }

    public Thread[] getGateThreads() {
        return gateThreads;
    }

    public void setGateThreads(Thread[] gateThreads) {
        this.gateThreads = gateThreads;
    }

    public Thread[] getPlaneThreads() {
        return airplaneThreads;
    }

    public void setPlaneThreads(Thread[] airplaneThreads) {
        this.airplaneThreads = airplaneThreads;
    }

    public BlockingQueue<Airplane> getQueue() {
        return Queue;
    }

    public void setQueue(BlockingQueue<Airplane> Queue) {
        this.Queue = Queue;
    }

    public ReentrantLock getRunway() {
        return runway;
    }

    public void setRunway(ReentrantLock runway) {
        this.runway = runway;
    }

    public ReentrantLock getDockinglane() {
        return dockinglane;
    }

    public void setDockinglane(ReentrantLock dockinglane) {
        this.dockinglane = dockinglane;
    } 

    public LaneControl getlc() {
        return lc;
    }

    public void setlc(LaneControl lanecontrol) {
        this.lc = lanecontrol;
    }
    

}
