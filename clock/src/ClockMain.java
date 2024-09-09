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

        //Thread för att hantera tids uppdatering, är just nu off med 1% för mycket
        Thread t1 = new Thread(() -> {
            while (true) {
                try {
                    long startTime = System.currentTimeMillis();
                
                    // Perform the update
                    tm.update();
                    
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long sleepTime = 1000 - elapsedTime;
                    
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
        });

        Thread t2 = new Thread(() -> {
            while (true) {
            }
        });

        t1.start();
        while (true) {
            //UserInput userInput = tm.getUserInput();
           // Choice c = userInput.choice();
        
           
        
           /*  switch (c) {
                case SET_TIME:
                    tm.setTime(); // bara skissar lite
                    break;
                case SET_ALARM:
                    tm.setAlarm(); 
                    break;
                case TOGGLE_ALARM:
                    tm.toggleAlarm();
                    break;
            } */
        
            /* int[] time = tm.getCurrentTime();
            for(int i = 0; i < 3; i++) {
               System.out.print(time[i] + " ");
            } */
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

        public TimeMonitor(ClockInput in, ClockOutput out) {
            this.in = in;
            this.out = out;

            this.hours = java.time.LocalTime.now().getHour();
            this.minutes = java.time.LocalTime.now().getMinute();
            this.seconds = java.time.LocalTime.now().getSecond();
            out.displayTime(hours, minutes, seconds);

            mutex = new Semaphore(1);
            free = new Semaphore(1);
            avail = in.getSemaphore();

        }

        public int getSeconds() throws InterruptedException {
            return seconds;
        }

        // metod för att uppdatera tiden i TimeMonitor
        public void update() throws InterruptedException {
            mutex.acquire();
            try {
                this.seconds = (this.seconds + 1) % 60;
                if (this.seconds == 0) {
                    this.minutes = (this.minutes + 1) % 60;
                    if (this.minutes == 0) {
                        this.hours = (this.hours + 1) % 24;
                    }
                }
                out.displayTime(this.hours, this.minutes, this.seconds);
            } finally {
                mutex.release();
            }
        }

        private UserInput getUserInput() {
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

        private void setUserInput() throws InterruptedException {
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

        //jag fuskar lite här men det står bara att inte returnera en referens till en Attribute array
        //returnerar en array med tids attributen
        public int[] getCurrentTime() throws InterruptedException {
            mutex.acquire();
            int[] time = new int[3];
            time[0] = hours;
            time[1] = minutes;
            time[2] = seconds;
            mutex.release();
            return time;
        }

    }
}
