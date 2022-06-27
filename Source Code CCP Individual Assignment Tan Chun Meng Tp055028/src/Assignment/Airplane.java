package Assignment;

import static Assignment.Main.gateAmount;
import static Assignment.Main.minutesToMilliseconds;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;
import static Assignment.Main.waitingTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

// airplane status
enum Status {
    NEW(0), QUEUE(1), LANDING(2), DOCKING(3), GATE(4), UNDOCKING(5), TAKEOFF(6), LEFT(7);

    private int phase;

    private Status(int phase) {
        this.phase = phase;
    }

    public int getPhase() {
        return phase;
    }

}

class Airplane implements Runnable {

    Random rnd = new Random();
    private String id;
    private int refuelingTime;
    private int landingTime;
    private long arrivingTime;
    private Airport airport;
    private AtomicReference<TerminalGate> assignedTGate;
    private ArrayBlockingQueue<Passenger> passengersOnBoard;
    private ArrayBlockingQueue<Thread> passengersThreadsOnBoard;
    private AtomicBoolean allowDisembark;
    private AtomicBoolean allowEmbark;
    private Semaphore gate;
    private SystemStatistic statistic;
    private volatile AtomicReference<Status> status;
    
    
    public String getAirplaneID() {
        return "Plane " + id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    public AtomicReference<TerminalGate> getAssignedGate() {
        return assignedTGate;
    }

    public void setAssignedGate(AtomicReference<TerminalGate> assignedTGate) {
        this.assignedTGate = assignedTGate;
    }

    public AtomicReference<Status> getStatus() {
        return status;
    }

    public void setStatus(AtomicReference<Status> status) {
        this.status = status;
    }

    public AtomicBoolean getAllowDisembark() {
        return allowDisembark;
    }

    public void setAllowDisembark(AtomicBoolean allowDisembark) {
        this.allowDisembark = allowDisembark;
    }

    public AtomicBoolean getAllowEmbark() {
        return allowEmbark;
    }

    public void setAllowEmbark(AtomicBoolean allowEmbark) {
        this.allowEmbark = allowEmbark;
    }
    
    public Semaphore getGate() {
        return gate;
    }

    public void setGate(Semaphore gate) {
        this.gate = gate;
    }
    
    public ArrayBlockingQueue<Passenger> getPassengersOnBoard() {
        return passengersOnBoard;
    }
    
    public SystemStatistic getSystemStatistic() {
        return statistic;
    }

    public void setSystemStatistic(SystemStatistic statistic) {
        this.statistic = statistic;
    }

    public Airplane(String id, Airport airport) {
        
    	this.id = id;
        this.airport = airport;
        arrivingTime = System.currentTimeMillis();
        landingTime = rnd.nextInt(5 * minutesToMilliseconds) + 1 * minutesToMilliseconds;
        refuelingTime = rnd.nextInt(waitingTime * 5) + 1 * minutesToMilliseconds;
        status = new AtomicReference<Status>(Status.NEW);
        assignedTGate = new AtomicReference<TerminalGate>();
        passengersOnBoard = new ArrayBlockingQueue<Passenger>(50);
        passengersThreadsOnBoard = new ArrayBlockingQueue<Thread>(50);
        allowDisembark = new AtomicBoolean(false);
        allowEmbark = new AtomicBoolean(false);
        gate = new Semaphore(2);
        statistic = new SystemStatistic();
         
        int currentNumberOfPassengers = rnd.nextInt(50);
        for (int i = 0; i < currentNumberOfPassengers; i++) {
            Passenger currentPassenger = new Passenger(this, 'L');
            passengersOnBoard.add(currentPassenger);

            Thread currentPassengerThread = new Thread(currentPassenger);
            passengersThreadsOnBoard.add(currentPassengerThread);
            currentPassengerThread.start();
        }
    }

    @Override
    public void run() {
    	
    	// thread will be alive until done execution
        while (status.get() != Status.LEFT) {
            // add new airplane into queue
            if (status.get() == Status.NEW) {
                addToQueue();
            }
            //check if the airplane is granted to land
            if (status.get() == Status.LANDING) {
                landing();
            }
        }
    }

    public synchronized void addToQueue() {
        try {
        	System.out.println(this.getAirplaneID() + " is requesting for landing");
        	
                while (!airport.getQueue().add(this)) {
                    System.err.println(this.getAirplaneID() + " is requesting for landing");
                };
                
                if (status.compareAndSet(Status.NEW, Status.QUEUE)) 
                {
                	System.out.println(this.getAirplaneID() + " is waiting to land");
                } 
                else {
                    System.err.println(this.getAirplaneID() + "'s status is illegal!");
                }
                if (assignedTGate.get() != null) {System.err.println(this.getAirplaneID() + "'s status is illegal!");}
        } catch (IllegalStateException e) {
            System.out.println("The airport is too packed. " + this.getAirplaneID() + " is flying to another airport");
            status.set(Status.LEFT);
        }
    }

    public synchronized void landing() {
        if (assignedTGate.get() != null) {
        	System.out.println(this.getAirplaneID() + " request granded");
        	System.out.println(this.getAirplaneID() + " is clear to land");
        	
            try {
                synchronized (assignedTGate.get()) {
                    if (assignedTGate.get().getName() != 'A') {
                        // assign airplane to gate B
                        if (!airport.getlc().getRunwayToDockinglane().tryAcquire(arrivingTime + refuelingTime - System.currentTimeMillis() - landingTime, TimeUnit.MILLISECONDS)) {
                            assignedTGate.get().notify();
                            assignedTGate.set(null);
                            return;
                        }
                    } else {
                    	// assign airplane to gate A
                        if (!airport.getlc().getRunwayToGate().tryAcquire(arrivingTime + refuelingTime - System.currentTimeMillis()- landingTime, TimeUnit.MILLISECONDS)) {
                            assignedTGate.get().notify();
                            assignedTGate.set(null);
                            return;
                        }
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }

            try {
                if (airport.getRunway().tryLock(arrivingTime + refuelingTime - System.currentTimeMillis() - landingTime, TimeUnit.MILLISECONDS)) {

                    System.out.println(this.getAirplaneID() + " is landing ........");
                    Thread.sleep(landingTime);

                    System.out.println(this.getAirplaneID() + " has landed sucessfully");
                    if (!status.compareAndSet(Status.LANDING, Status.DOCKING)) {
                        System.out.println(this.getAirplaneID() + "error");
                        airport.getRunway().unlock();
                    }
                } else {
                	
                    if (assignedTGate.get() != null) 
                    {
                    	assignedTGate.get().notifyAll();
                    }
                    airport.getlc().getRunwayToDockinglane().release();
                    return;
                }
            } catch (InterruptedException ex) {
                airport.getlc().getRunwayToDockinglane().release();
                ex.printStackTrace();
                return;
            }
            docking();
        }

    }

    public synchronized void docking() {

        //Use docking lane if not first gate
        if (assignedTGate.get().getName() != 'A') {

            airport.getRunway().unlock();
            System.out.println("Runway is clear to go");

            System.out.println(this.getAirplaneID() + " is on the way to the docking lane");
            try {
                Thread.sleep(2 * minutesToMilliseconds);
            } catch (InterruptedException ex) {
                airport.getlc().getRunwayToDockinglane().release();
                ex.printStackTrace();
                return;
            }

            try {
                //Check if from docking lane to gate is free before locking the docking lane
                airport.getlc().getDockinglaneToGate().acquire();
                System.out.println("The docking lane to " + assignedTGate.get().getGateID()+" is good to use" );

            } catch (InterruptedException ex) {
                airport.getlc().getRunwayToDockinglane().release();
                ex.printStackTrace();
                return;
            }

            airport.getDockinglane().lock();
            System.out.println(this.getAirplaneID() + " is using the docking lane.");
            airport.getlc().getRunwayToDockinglane().release();

            try {
                Thread.sleep(1 * minutesToMilliseconds);
                System.out.println(this.getAirplaneID() + " is on the way to " + assignedTGate.get().getGateID());
            } catch (InterruptedException ex) {
                airport.getlc().getDockinglaneToGate().release();
                ex.printStackTrace();
                return;
            } finally {
                airport.getDockinglane().unlock();
            }

            try {
                Thread.sleep(2 * minutesToMilliseconds);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            } finally {
                airport.getlc().getDockinglaneToGate().release();
            }

        } else {
            System.out.println(this.getAirplaneID() + " is on the way to " + assignedTGate.get().getGateID());
            System.out.println("Runway is clear to go");
            airport.getRunway().unlock();

            try {
                Thread.sleep(2 * minutesToMilliseconds);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            } finally {
                airport.getlc().getRunwayToGate().release();
            }
        }

        int dockingTime = rnd.nextInt(4 * minutesToMilliseconds);
        try {
            Thread.sleep(dockingTime);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return;
        }
        System.out.println(this.getAirplaneID() + " has docked to " + assignedTGate.get().getGateID() + " successfully");
        if (!status.compareAndSet(Status.DOCKING, Status.GATE)) {
            System.out.println(this.getAirplaneID() + "error");

        }
        disembark();

    }

    public synchronized void disembark() {
        try {
        	 int engineshutdown = (rnd.nextInt(3 * minutesToMilliseconds));
        	 
        	 
        	 System.out.println(this.getAirplaneID() + " engine shuting down...........");
             Thread.sleep(engineshutdown);
            //Concurrent, will be working throughout the process
            Thread refillFuel = new Thread(new AirportWorking(this.getAirplaneID(), "refill fuel", 51, 10));
            refillFuel.start();

            //Concurrent
            //Allow disembarking
            System.out.println(this.getAirplaneID() + " good to disembark");
            System.out.println( passengersOnBoard.size() + " passengers disembarking .......");
            if (allowDisembark.compareAndSet(false, true)) {

                for (Passenger p : passengersOnBoard) {
                    synchronized (p) {
                        p.notify();
                    }
                }
                //Wait until passengers are all disembarked
                while (passengersOnBoard.size() != 0) {

                }
                System.out.println(this.getAirplaneID() + " passengers disembarked completed");
            }

            //Sequential, starts right after all the passengers disembarked.
            Thread cleanCabin = new Thread(new AirportWorking(this.getAirplaneID(), "clean cabin", 21, 10));
            Thread refillSupplies = new Thread(new AirportWorking(this.getAirplaneID(), "supplies refilling", 21, 10));
            cleanCabin.start();
            refillSupplies.start();

            //Sequential, starts right after all the passengers disembarked.
            cleanCabin.join();
            refillSupplies.join();

            //Sequential
            //Allow passenger to embark
            int count = airport.PassengerAmount(id);
            System.out.println(this.getAirplaneID() + " is good to embark passager");
            System.out.println(count + " passengers is ready to embarked to "+ this.getAirplaneID());
            System.out.println("passengers embarking.......");
            
            if (allowEmbark.compareAndSet(false, true)) {
                for (Passenger p : airport.getWaitingList(id)) {
                    synchronized (p) {
                        p.notify();
                    }
                }
            }

            //Wait until passengers are all embarked
            while (count != passengersOnBoard.size() && passengersOnBoard.size() < 50) {

            }
            System.out.println(this.getAirplaneID() + " passenger embarked completed");
            System.out.println(this.getAirplaneID() + " Canbin Crew is embarked ");

            //Wait for fuel to complete refuel
            refillFuel.join();
            
            System.out.println(this.getAirplaneID() + " engine starting...........");
            Thread.sleep(engineshutdown);
            System.out.println(this.getAirplaneID() + " is requesting for to undock!");
            
            if (!status.compareAndSet(Status.GATE, Status.UNDOCKING)) {
                System.out.println(this.getAirplaneID() + "error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        undocking();
    }

    public synchronized void undocking() {

        int undockingTime = (rnd.nextInt(4 * minutesToMilliseconds));

        if (assignedTGate.get().getName() != 'A' + gateAmount - 1) {
            synchronized (assignedTGate.get()) {
                try {
                    airport.getlc().getGateToDockinglane()[assignedTGate.get().getName() - 'A'].acquire();
                    System.out.println("The docking lane is good to undock");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                try {
                    //Aircraft is undocking
                    Thread.sleep(undockingTime);
                    System.out.println(this.getAirplaneID() + " is undocking......");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                System.out.println(this.getAirplaneID() + " has undocked from " + assignedTGate.get().getGateID());
                try {
                    assignedTGate.get().notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println(this.getAirplaneID() + " is on the way to the docking lane from " + assignedTGate.get().getGateID());
            try {
                Thread.sleep(2 * minutesToMilliseconds);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }

            try {
                //Check if from intersection to standby is free before locking the intersection
                airport.getlc().getDockinglaneToTakeofflane().acquire();
                System.out.println("The way to take-off from docking lane is good to use");

            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }

            airport.getDockinglane().lock();
            System.out.println(this.getAirplaneID() + " is on the docking lane to take off");
            airport.getlc().getGateToDockinglane()[assignedTGate.get().getName() - 'A'].release();

            try {
                Thread.sleep(1 * minutesToMilliseconds); //Going through intersection
                System.out.println(this.getAirplaneID() + " is on the way to the take-off runway");
            } catch (InterruptedException ex) {
                airport.getlc().getDockinglaneToTakeofflane().release();
                ex.printStackTrace();
                return;
            } finally {
                airport.getDockinglane().unlock();
            }

            try {
                Thread.sleep(2 * minutesToMilliseconds);
                airport.getlc().getReadyTakeoff().acquire();
                System.out.println(this.getAirplaneID() + " is waiting to take-off");
                System.out.println(this.getAirplaneID() + " is requesting to take-off");

            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            } finally {
                airport.getlc().getDockinglaneToTakeofflane().release();
            }

        } else {

            synchronized (assignedTGate.get()) {
                try {
                    airport.getlc().getGateToTakeofflane().acquire();
                    System.out.println("The way to take-off from "+ assignedTGate.get().getGateID() +" is good to use");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return;
                }

                try {
                    //Aircraft is undocking
                	System.out.println(this.getAirplaneID() + " is undocking..... ");
                    Thread.sleep(undockingTime);

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return;
                }

                System.out.println(this.getAirplaneID() + " has undocked from " + assignedTGate.get().getGateID());
                assignedTGate.get().notify();
            }
            System.out.println(this.getAirplaneID() + " is on the way to take-off runway from " + assignedTGate.get().getGateID());
            try {
                Thread.sleep(2 * minutesToMilliseconds);
                airport.getlc().getReadyTakeoff().acquire();
                System.out.println(this.getAirplaneID() + " is waiting to take-off.");
                System.out.println(this.getAirplaneID() + " is requesting to take-off");

            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            } finally {
                airport.getlc().getGateToTakeofflane().release();
            }

        }
        if (!status.compareAndSet(Status.UNDOCKING, Status.TAKEOFF)) {
            System.out.println(this.getAirplaneID() + "error");
        }
        airport.getRunway().lock();
        airport.getlc().getReadyTakeoff().release();
        System.out.println(this.getAirplaneID() + " request granded");
        System.out.println(this.getAirplaneID() + "'s turn to use the runway");
        takeOff();
    }

    public synchronized void takeOff() {
        try {
        	int boostengineTime = (rnd.nextInt(5 * minutesToMilliseconds) + 1 * minutesToMilliseconds);
            int takeOffTime = (rnd.nextInt(3 * minutesToMilliseconds) + 1 * minutesToMilliseconds);
            
        	System.out.println(this.getAirplaneID() + " engine boosting up.........");
            Thread.sleep(boostengineTime);
        	System.out.println(this.getAirplaneID() + " is clear to take off");
            Thread.sleep(takeOffTime);
            System.out.println(this.getAirplaneID() + " take off successfully");
            statistic.AirplaneStatisticTime(arrivingTime, System.currentTimeMillis());
            if (!status.compareAndSet(Status.TAKEOFF, Status.LEFT)) {
                System.out.println(this.getAirplaneID() + "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            System.out.println("Runway is good to go");
            airport.getRunway().unlock();

        }
    }
   
   

}
