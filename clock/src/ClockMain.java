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

        // Thread för att hantera tids uppdatering, är just nu off med 1% för mycket
        Thread t1 = new Thread(() -> {
            long t, diff;
            t = System.currentTimeMillis();

            while (true) {
                try {

                    // Updaterar tiden
                    tm.update();

                    // Alarm check
                    tm.alarmCheck();

                    t += 1000;
                    diff = t - System.currentTimeMillis();

                    if (diff > 0) {
                        Thread.sleep(diff);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        Thread t2 = new Thread(() -> {
            while (true) {
                tm.getUserInput();
            }
        });

        t1.start();
        t2.start();
    }

    public static class TimeMonitor {
        // Clock Simulator
        private ClockInput in;
        private ClockOutput out;

        // Reala TidsAttribut
        private int hours;
        private int minutes;
        private int seconds;

        // Alarm TidsAttribut + Alarm On/Off & Alarm Counter
        private int alarmHours;
        private int alarmMinutes;
        private int alarmSeconds;
        private int alarmCounter;
        private boolean alarmOn;

        // Semaphores
        private Semaphore mutex;
        private Semaphore avail;

        public TimeMonitor(ClockInput in, ClockOutput out) {
            this.in = in;
            this.out = out;

            this.hours = java.time.LocalTime.now().getHour();
            this.minutes = java.time.LocalTime.now().getMinute();
            this.seconds = java.time.LocalTime.now().getSecond();
            out.displayTime(hours, minutes, seconds);

            mutex = new Semaphore(1);
            avail = in.getSemaphore();

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

        public void alarmCheck() throws InterruptedException {
            mutex.acquire();
            try {
                if (alarmOn && hours == alarmHours && minutes == alarmMinutes
                        && seconds == alarmSeconds) {
                    alarmCounter = 20;
                }

                if (alarmCounter > 0) {
                    out.alarm();
                    alarmCounter--;
                }
            } finally {
                mutex.release();
            }
        }

        public void getUserInput() {
            try {
                avail.acquire();
                checkUserChoice(in.getUserInput());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void checkUserChoice(UserInput userInput) throws InterruptedException {
            Choice c = userInput.choice();

            switch (c) {
                case SET_TIME:
                    setTime(userInput);
                    break;
                case SET_ALARM:
                    setAlarm(userInput);
                    break;
                case TOGGLE_ALARM:
                    toggleAlarm(userInput);
                    break;
            }
        }

        private void toggleAlarm(UserInput userInput) {
            try {
                mutex.acquire();
                alarmOn = !alarmOn;
                out.setAlarmIndicator(alarmOn);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mutex.release();
            }
        }

        private void setAlarm(UserInput userInput) {
            try {
                mutex.acquire();
                this.alarmHours = userInput.hours();
                this.alarmMinutes = userInput.minutes();
                this.alarmSeconds = userInput.seconds();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mutex.release();
            }
        }

        private void setTime(UserInput userInput) {
            try {
                mutex.acquire();
                this.hours = userInput.hours();
                this.minutes = userInput.minutes();
                this.seconds = userInput.seconds();
                out.displayTime(this.hours, this.minutes, this.seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mutex.release();
            }
        }

    }
}
