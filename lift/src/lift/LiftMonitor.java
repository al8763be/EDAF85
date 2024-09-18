package lift;

import java.util.Arrays;

public class LiftMonitor {

    private int currentFloor; // the floor the lift is currently on
    private boolean moving; // true if the lift is moving, false if standing still with doors open
    private boolean directionUp; // true if lift is going up, false if going down
    private int[] waitEntry; // number of passengers waiting to enter the lift at the various floors
    private int[] waitExit; // number of passengers (in lift) waiting to leave at the various floors
    private int load; // number of passengers currently in the lift
    private LiftView view; // view object passed from main
    private boolean doorOpen; // State (open/closed) of door on current floor
    private int passengersEntering; // Counter of number of passengers currently entering the elevator
    private int passengersExiting; // Counter of number of passengers currently exiting the elevator
    private final int NBR_FLOORS; // Number of floors
    private final int MAX_PASSENGERS; // Maximum number of passengers

    public LiftMonitor(LiftView v, int nbrFloors, int maxPassengers) {
        this.currentFloor = 0;
        this.moving = true;
        this.directionUp = true;
        this.NBR_FLOORS = nbrFloors;
        this.MAX_PASSENGERS = maxPassengers;
        this.waitEntry = new int[nbrFloors];
        this.waitExit = new int[nbrFloors];
        this.load = 0;
        this.view = v;
        this.passengersEntering = 0;
        this.passengersExiting = 0;
    }

    // Increases the number of waiting passengers on given floor
    public synchronized void increaseWaitEntry(int passengerFloor) {
        waitEntry[passengerFloor]++;
        notifyAll();
    }

    // Handles passengers waiting to enter arriving lift
    public synchronized void enterLift(int passengerFloor, int passengerDestination) {
        while (currentFloor != passengerFloor || load == MAX_PASSENGERS || moving || passengersEntering == MAX_PASSENGERS) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.enterLift interrupted " + e);
            }
        }
        waitEntry[currentFloor]--;
        waitExit[passengerDestination]++;
        load++;
        passengersEntering++;
        notifyAll();
    }

    // Called after passengers has completed entry animation
    public synchronized void enterCompleted() {
        passengersEntering--;
        notifyAll();
    }

    // Handles passengers waiting to exit lift
    public synchronized void exitLift(int passengerDestination) {
        while (currentFloor != passengerDestination) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.exitLift interrupted " + e);
            }
        }
        waitExit[passengerDestination]--;
        load--;
        passengersExiting++;
        notifyAll();
    }

    // Called after passengers has completed exit animation
    public synchronized void exitCompleted() {
        passengersExiting--;
        notifyAll();
    }

    // Handles the conditions when the elevator is to wait for passengers entering/exiting the lift and waiting when there are no passengers waiting on any floor.
    public synchronized int[] liftContinue() {
        // Wait until there are any passengers waiting to enter or exit
        while (Arrays.stream(waitEntry).sum() == 0 && Arrays.stream(waitExit).sum() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("LiftContinue no passengers interrupted " + e);
            }
        }
    
        // If there are passengers to exit or enter on current floor, open doors
        if ((waitExit[currentFloor] > 0 || (waitEntry[currentFloor] > 0 && load < MAX_PASSENGERS)) && !doorOpen) {
            view.openDoors(currentFloor);
            doorOpen = true;
            notifyAll();
        }
    
        // Wait while passengers are entering or exiting
        while (passengersEntering > 0 || passengersExiting > 0 || 
               (doorOpen && (waitExit[currentFloor] > 0 || (waitEntry[currentFloor] > 0 && load < MAX_PASSENGERS)))) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.liftContinue interrupted " + e);
            }
        }
    
        // Close doors if they are open
        if (doorOpen) {
            doorOpen = false;
            view.closeDoors();
        }
    
        // Move the lift
        moving = true;
        calculateDirection();
        int[] movingPositions = new int[2];
        movingPositions[0] = currentFloor;
        movingPositions[1] = currentFloor + (directionUp ? 1 : -1);
        return movingPositions;
    }
    
    public synchronized void arrived() {
        moving = false;
        notifyAll();
    }
    

    // Moves the elevator in the given direction
    public synchronized void incrementFloor() {
        currentFloor = currentFloor + (directionUp ? 1 : -1);
    }

    // Handles calculation of elevator direction
    // Changes the direction of the elevator if there are no passengers that want to enter or exit on floors either above or below the elevator
    private void calculateDirection() {
        if (Arrays.stream(waitEntry, currentFloor, NBR_FLOORS).sum() == 0 && Arrays.stream(waitExit, currentFloor, NBR_FLOORS).sum() == 0 && currentFloor != 0) {
            directionUp = false;
        } else if (Arrays.stream(waitEntry, 0, currentFloor + 1).sum() == 0 && Arrays.stream(waitExit, 0, currentFloor + 1).sum() == 0 && currentFloor != NBR_FLOORS - 1) {
            directionUp = true;
        } else if (currentFloor == NBR_FLOORS - 1) {
            directionUp = false;
        } else if (currentFloor == 0) {
            directionUp = true;
        }
    }
}