package lift;

public class Main {
    public static void main(String[] args) {
        final int NBR_FLOORS = 7, MAX_PASSENGERS = 4;

        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        LiftMonitor m = new LiftMonitor(view, NBR_FLOORS,MAX_PASSENGERS);
        LiftThread lift = new LiftThread(m,view);
    
        for (int i = 0; i < 4   ; i++) {
            PassengerThread p = new PassengerThread(m, view);
            new Thread(p).start();
        }
        new Thread(lift).start();
    }
}
