package lift;

public class PassengerThread implements Runnable {
    private LiftMonitor m;
    private LiftView v;

    public PassengerThread(LiftMonitor m, LiftView v) {
        this.m = m;
        this.v = v;
    }

    @Override
    public void run() {
        Passenger p = v.createPassenger();
        while (true) {
            p.begin();
            int startFloor = p.getStartFloor();
            int destinationFloor = p.getDestinationFloor();

            // Increase the number of passengers waiting to enter at the start floor
            m.increaseWaitEntry(startFloor);

            // Enter the lift
            m.enterLift(startFloor, destinationFloor);
            p.enterLift();
            m.enterCompleted(); 

            // Exit the lift
            m.exitLift(destinationFloor);
            p.exitLift();
            m.exitCompleted();

            p.end();
            run();
        }
    }
}