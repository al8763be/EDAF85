import java.util.concurrent.Semaphore;
import clock.AlarmClockEmulator;
import clock.io.Choice;
import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockOutput;

public class ClockMain {
    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput in = emulator.getInput();
        ClockOutput out = emulator.getOutput();
        TimeMonitor tm = new TimeMonitor(in, out);

        // out.displayTime(15, 2, 37); // arbitrary time: just an example

        while (true) {
            UserInput userInput = tm.getUserInput();
            Choice c = userInput.choice();

            Thread t1 = new Thread(() -> {
                try {
                    tm.update();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            switch (c) {
                case SET_TIME:
                    tm.setTime(); // bara skissar lite
                    break;
                case SET_ALARM:
                    tm.setAlarm(); 
                    break;
                case TOGGLE_ALARM:
                    tm.toggleAlarm();
                    break;
            }

            int h = userInput.hours();
            int m = userInput.minutes();
            int s = userInput.seconds();

            System.out.println("choice=" + c + " h=" + h + " m=" + m + " s=" + s);
        }
    }

    public static class TimeMonitor {
        private ClockInput in;
        private ClockOutput out;
        private int hours;
        private int minutes;
        private int seconds;
        private Semaphore mutex;
        private Semaphore free;
        private Semaphore avail;

        // Något något system.Time - 1000 ms

        public TimeMonitor(ClockInput in, ClockOutput out) {
            this.in = in;
            this.out = out;

            this.hours = java.time.LocalTime.now().getHour();
            this.minutes = java.time.LocalTime.now().getMinute();
            this.seconds = java.time.LocalTime.now().getSecond();

            mutex = new Semaphore(1);
            free = new Semaphore(1);
            avail = new Semaphore(0);

        }

        public void update() {
            // Ta nuvarnade tid 
            
            // 
            throw new UnsupportedOperationException("Unimplemented method 'update'");
        }

        public UserInput getUserInput() {
            try {
                avail.acquire();
                UserInput userInput = in.getUserInput();
                avail.release();
                return userInput;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void setUserInput() throws InterruptedException {
            UserInput userInput = getUserInput();

            mutex.acquire();
            hours = userInput.hours();
            minutes = userInput.minutes();
            seconds = userInput.seconds();
            out.displayTime(hours, minutes, seconds);
            mutex.release();
        }

        public void changeTime() {

        }

        public void getTime() {

        }

    }
}
