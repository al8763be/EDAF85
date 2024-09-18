package lift;

public class LiftThread implements Runnable {
    private LiftMonitor m;
    private LiftView v;
    private int currentFloor = 0;

    public LiftThread(LiftMonitor m, LiftView v) {
        this.m = m;
        this.v = v;
    }

    @Override
    public void run() {

        while (true) {      
            int[] positions = m.liftContinue();
            v.moveLift(currentFloor, positions[1]);
            m.incrementFloor();
            currentFloor = positions[1];
            m.arrived();
        }
    }

}
