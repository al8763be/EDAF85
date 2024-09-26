package lift;

public class PassengerThread extends Thread {
    private LiftMonitor lift;
    private LiftView view;

    public PassengerThread(LiftMonitor lift, LiftView view) {
        this.view = view;
        this.lift = lift;
    }

    @Override
    public void run() {
        while (true) {
            Passenger passenger = view.createPassenger();
            passenger.begin();
            // Increase the number of passengers waiting to enter at the start floor
            lift.increaseWaitEntry(passenger.getStartFloor());

            // Enter the lift
            lift.enterLift(passenger);
            passenger.enterLift();
            lift.enterCompleted();

            // // Exit the lift
            lift.exitLift(passenger);
            passenger.exitLift();
            lift.exitCompleted();

            passenger.end();
        }
    }
}