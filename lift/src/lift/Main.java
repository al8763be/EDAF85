package lift;

public class Main {
    public static void main(String[] args) {
        final int NBR_FLOORS = 7, MAX_PASSENGERS = 4, NUM_PASSENGERS = 20;

        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        LiftMonitor monitor = new LiftMonitor(view, NBR_FLOORS, MAX_PASSENGERS);
        LiftThread lift = new LiftThread(monitor, view);

        for (int i = 0; i < NUM_PASSENGERS; i++) {
            PassengerThread p = new PassengerThread(monitor, view);
            new Thread(p).start();
        }
        new Thread(lift).start();
    }
}