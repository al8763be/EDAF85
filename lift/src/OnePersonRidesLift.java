
import lift.LiftView;
import lift.Passenger;

public class OnePersonRidesLift {

    public static void main(String[] args) {

        final int NBR_FLOORS = 7, MAX_PASSENGERS = 4;

        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        Passenger pass = view.createPassenger();
        int fromFloor = pass.getStartFloor();
        int toFloor = pass.getDestinationFloor();

        pass.begin(); // walk in (from left)
        if (fromFloor != 0) {
            view.moveLift(0, fromFloor);
        }
        view.openDoors(fromFloor);
        pass.enterLift(); // step inside

        view.closeDoors();
        view.moveLift(fromFloor, toFloor); // ride lift
        view.openDoors(toFloor);

        pass.exitLift(); // leave lift
        pass.end(); // walk out (to the right)
    }
}