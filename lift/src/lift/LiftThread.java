package lift;

public class LiftThread implements Runnable {
    private LiftMonitor lift;
    private LiftView view;
    private int currentFloor = 0;

    public LiftThread(LiftMonitor lift, LiftView view) {
        this.lift = lift;
        this.view = view;
    }

    @Override
    public void run() {

        while (true) {      
            int[] positions = lift.moveLift();
            view.moveLift(currentFloor, positions[1]);
            lift.incrementFloor();
            currentFloor = positions[1];
            lift.arrived();
        }
    }

}