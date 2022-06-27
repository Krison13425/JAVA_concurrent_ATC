package Assignment;



import java.util.concurrent.Semaphore;

public class LaneControl {
	
    private Semaphore RunwayToDockinglane;
    private Semaphore RunwayToGate;
    private Semaphore DockinglaneToGate;
    private Semaphore[] GateToDockinglane;
    private Semaphore DockinglaneToTakeofflane;
    private Semaphore GateToTakeofflane;
    private Semaphore ReadyTakeoff;

    public Semaphore getRunwayToDockinglane() {
        return RunwayToDockinglane;
    }

    public void setRunwayToDockinglane(Semaphore RunwayToDockinglane) {
        this.RunwayToDockinglane = RunwayToDockinglane;
    }

    public Semaphore getRunwayToGate() {
        return RunwayToGate;
    }

    public void setRunwayToGate(Semaphore RunwayToGate) {
        this.RunwayToGate = RunwayToGate;
    }

    public Semaphore getDockinglaneToGate() {
        return DockinglaneToGate;
    }

    public void setDockinglaneToGate(Semaphore DockinglaneToGate) {
        this.DockinglaneToGate = DockinglaneToGate;
    }

    public Semaphore[] getGateToDockinglane() {
        return GateToDockinglane;
    }

    public void setGateToDockinglane(Semaphore[] GateToDockinglane) {
        this.GateToDockinglane = GateToDockinglane;
    }
            
    public void setGateAToDockinglane(int i, Semaphore gateAToDockinglane) {
    	GateToDockinglane[i] = gateAToDockinglane;
    }

    public Semaphore getDockinglaneToTakeofflane() {
        return DockinglaneToTakeofflane;
    }

    public void setDockinglaneToTakeofflane(Semaphore DockinglaneToTakeofflane) {
        this.DockinglaneToTakeofflane = DockinglaneToTakeofflane;
    }

    public Semaphore getGateToTakeofflane() {
        return GateToTakeofflane;
    }

    public void setGateToTakeofflane(Semaphore GateToTakeofflane) {
        this.GateToTakeofflane = GateToTakeofflane;
    }

    public Semaphore getReadyTakeoff() {
        return ReadyTakeoff;
    }

    public void setReadyTakeoff(Semaphore ReadyTakeoff) {
        this.ReadyTakeoff = ReadyTakeoff;
    }
    
    
}
