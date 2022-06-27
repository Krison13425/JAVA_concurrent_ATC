package Assignment;



public class Main {

	
    public static int gateAmount = 2;
    public static int minutesToMilliseconds = 60;
    public static int waitingTime = 60 * minutesToMilliseconds;
    
    
    public static void main(String[] args) {

        Airport airport = new Airport(gateAmount, 2, 3, 6);
        airport.run();
    }
}
