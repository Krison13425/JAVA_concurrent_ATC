package Assignment;

import static Assignment.Main.minutesToMilliseconds;
import java.util.Random;
class AirportWorking implements Runnable {

    private String name;
    private String airplaneID;
    private int rangeInMinutes;
    private int minTimeInMinutes;
    private int duration = 0;

    public AirportWorking(String airplaneID,String name, int rangeInMinutes, int minTimeInMinutes) {
        this.name = name;
        this.airplaneID = airplaneID;
        this.rangeInMinutes = rangeInMinutes;
        this.minTimeInMinutes = minTimeInMinutes;
    }

    @Override
    public void run() {
        try {
            System.out.println(airplaneID + " " +name + " in progress.....");
            duration = new Random().nextInt(rangeInMinutes * minutesToMilliseconds) + minTimeInMinutes * minutesToMilliseconds;
            Thread.sleep(duration);
            System.out.println(airplaneID+" completed " + name );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
