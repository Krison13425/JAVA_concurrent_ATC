package Assignment;

import java.util.concurrent.atomic.AtomicReference;

class TerminalGate implements Runnable {

    private char name;
    private Airport airport;
    private volatile AtomicReference<Airplane> currAirplane;

    public TerminalGate(char name, Airport airport) {
        this.name = name;
        this.airport = airport;
        currAirplane = new AtomicReference<Airplane>();
    }

    @Override
    public void run() {
        try {
            while (airport.getIsRunning().get()) {
            	
                if (currAirplane.get() == null) 
                {
                	currAirplane.set(airport.getQueue().poll());
                } 

                if (currAirplane.get() != null) {
                    //Ensure that the aircraft does not left the airport
                    assigngate();
                }
                Thread.sleep(100);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assigngate() {
        try {
            synchronized (this) {
                if (currAirplane.get().getStatus().compareAndSet(Status.QUEUE, Status.LANDING)) {
                    System.out.println(currAirplane.get().getAirplaneID()+ " is assigned to dock at " + this.getGateID() );
                    currAirplane.get().getAssignedGate().set(this);
                    this.wait();
                    //Will be notified by aircraft
                    currAirplane.set(null);
                    System.out.println(this.getGateID() + " is available.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public char getName() {
        return name;
    }

    public void setName(char name) {
        this.name = name;
    }

    public String getGateID() {
        return "Terminal Gate " + name;
    }

}
