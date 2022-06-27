
package Assignment;

import java.util.concurrent.atomic.AtomicInteger;

public class SystemStatistic {
    private long arrivingTime;
    private long takeoffTime;
    private AtomicInteger embarkedPassenger=new AtomicInteger(0);
    private AtomicInteger disembarkedPassenger=new AtomicInteger(0);
    private boolean doneExecute=false;


    public void AirplaneStatisticTime(long arrivingTime, long takeoffTime){
        this.arrivingTime=arrivingTime;
        this.takeoffTime=takeoffTime;
        this.doneExecute=true;
    }
    
    public int EmbarkedPassenger() {
        synchronized(embarkedPassenger){
                return embarkedPassenger.incrementAndGet();
        }
    }
    
     public int DisembarkedPassenger() {
        synchronized(disembarkedPassenger){
                return disembarkedPassenger.incrementAndGet();
        }
    }

    public long getArrivingTime() {
        return arrivingTime;
    }


    public long getTakeoffTime() {
        return takeoffTime;
    }

    public AtomicInteger getEmbarkedPassenger() {
        return embarkedPassenger;
    }

    public AtomicInteger getDisembarkedPassenger() {
        return disembarkedPassenger;
    }

    public boolean Doneexecute() {
        return doneExecute;
    }
     
     
}