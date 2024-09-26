package lift;

import java.util.*;

public class LiftMonitor {
    private int currentFloor; // the floor the lift is currently on
    private boolean isMoving; // true if the lift is moving, false if standing still with doors open
    private boolean goingUp; // true if lift is going up, false if going down
    private int[] toEnter; // number of passengers waiting to enter the lift at the various floors
    private int[] toExit; // number of passengers (in lift) waiting to leave at the various floors
    private int numPassengers; // number of passengers currently in the lift
    private LiftView view; // view object passed from main

    private boolean doorOpen; // State (open/closed) of door on current floor
    private int passengersEntering; // Counter of number of passengers currently entering the elevator
    private int passengersExiting; // Counter of number of passengers currently exiting the elevator

    private final int NBR_FLOORS; // Number of floors
    private final int MAX_PASSENGERS; // Maximum number of passengers

    public LiftMonitor(LiftView view, int nbrFloors, int maxPassengers) {
        this.currentFloor = 0;
        this.doorOpen = false;
        this.isMoving = false;
        this.goingUp = true;
        this.NBR_FLOORS = nbrFloors;
        this.MAX_PASSENGERS = maxPassengers;
        this.toEnter = new int[nbrFloors];
        this.toExit = new int[nbrFloors];
        this.numPassengers = 0;
        this.view = view;
        this.passengersEntering = 0;
        this.passengersExiting = 0;
    }

    // Increases the number of waiting passengers on given floor
    public synchronized void increaseWaitEntry(int passengerFloor) {
        toEnter[passengerFloor]++;
        notifyAll();
    }

    // Handles passengers waiting to enter arriving lift
    public synchronized void enterLift(Passenger passenger) {
        int passengerFloor = passenger.getStartFloor();
        while (currentFloor != passengerFloor || numPassengers >= MAX_PASSENGERS || isMoving
                || passengersEntering + numPassengers == MAX_PASSENGERS || !doorOpen) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.enterLift interrupted " + e);
            }
        }
        System.out.println("Passenger entering at floor: " + currentFloor);
        toEnter[currentFloor]--;
        toExit[passenger.getDestinationFloor()]++;
        numPassengers++;
        passengersEntering++;
        notifyAll();
    }

    // Called after passengers has completed entry animation
    public synchronized void enterCompleted() {
        passengersEntering--;
        System.out.println("Passenger entry completed at floor: " + currentFloor);
        notifyAll();
    }

    // Handles passengers waiting to exit lift
    public synchronized void exitLift(Passenger passenger) {
        int passengerDestFloor = passenger.getDestinationFloor();
        while (currentFloor != passengerDestFloor || !doorOpen) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.exitLift interrupted " + e);
            }
        }
        System.out.println("Passenger exiting at floor: " + currentFloor);
        toExit[passengerDestFloor]--;
        numPassengers--;
        passengersExiting++;
        notifyAll();
    }

    // Called after passengers has completed exit animation
    public synchronized void exitCompleted() {
        passengersExiting--;
        System.out.println("Passenger exit completed at floor: " + currentFloor);
        notifyAll();
    }

    // Handles the conditions when the elevator is to wait for passengers
    // entering/exiting the lift and waiting when there are no passengers waiting on
    // any floor.
    public synchronized int[] moveLift() {
        // Wait until there are any passengers waiting to enter or exit
        while (Arrays.stream(toEnter).sum() == 0 && Arrays.stream(toExit).sum() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("LiftContinue no passengers interrupted " + e);
            }
        }

        // If there are passengers to exit or enter on current floor, open doors
        if ((toExit[currentFloor] > 0 || (toEnter[currentFloor] > 0 && numPassengers < MAX_PASSENGERS)) && !doorOpen) {
            view.openDoors(currentFloor);
            doorOpen = true;
            System.out.println("Doors opened at floor: " + currentFloor);
            notifyAll();
        }

        // Wait while passengers are entering or exiting
        while (passengersEntering > 0 || passengersExiting > 0 ||
                (doorOpen && (toExit[currentFloor] > 0
                        || (toEnter[currentFloor] > 0 && numPassengers < MAX_PASSENGERS)))) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("LiftContinue entering/exiting interrupted " + e);
            }
        }

        // Close doors if they are open
        if (doorOpen) {
            doorOpen = false;
            view.closeDoors();
            System.out.println("Doors closed at floor: " + currentFloor);
        }

        // If there are no passengers waiting to enter or exit on current floor, stand still and run moveLift again
        if (Arrays.stream(toEnter).sum() == 0 && Arrays.stream(toExit).sum() == 0) {
            return moveLift();
        }

        // Move the lift
        isMoving = true;
        calculateDirection();
        int[] movingPositions = new int[2];
        movingPositions[0] = currentFloor;
        movingPositions[1] = currentFloor + (goingUp ? 1 : -1);
        System.out.println("Lift moving from floor " + movingPositions[0] + " to floor " + movingPositions[1]);
        return movingPositions;
    }

    public synchronized void arrived() {
        isMoving = false;
        System.out.println("Lift arrived at floor: " + currentFloor);
        notifyAll();
    }

    // Moves the elevator in the given direction
    public synchronized void incrementFloor() {
        currentFloor = currentFloor + (goingUp ? 1 : -1);
        System.out.println("Lift incremented to floor: " + currentFloor);
    }

    // Handles calculation of elevator direction
    // Changes the direction of the elevator if there are no passengers that want to
    // enter or exit on floors either above or below the elevator
    private void calculateDirection() {
        if (Arrays.stream(toEnter, currentFloor, NBR_FLOORS).sum() == 0
                && Arrays.stream(toExit, currentFloor, NBR_FLOORS).sum() == 0 && currentFloor != 0) {
            goingUp = false;
        } else if (Arrays.stream(toEnter, 0, currentFloor + 1).sum() == 0
                && Arrays.stream(toExit, 0, currentFloor + 1).sum() == 0 && currentFloor != NBR_FLOORS - 1) {
            goingUp = true;
        } else if (currentFloor == NBR_FLOORS - 1) {
            goingUp = false;
        } else if (currentFloor == 0) {
            goingUp = true;
        }
        System.out.println("Lift direction calculated: " + (goingUp ? "up" : "down"));
    }
}