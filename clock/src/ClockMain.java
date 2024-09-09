import clock.AlarmClockEmulator;
import clock.io.Choice;
import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockOutput;

public class ClockMain {
    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput  in  = emulator.getInput();
        ClockOutput out = emulator.getOutput();

        out.displayTime(15, 2, 37);   // arbitrary time: just an example

        while (true) {
            UserInput userInput = in.getUserInput();
            Choice c = userInput.choice();
            int h = userInput.hours();
            int m = userInput.minutes();
            int s = userInput.seconds();

            System.out.println("choice=" + c + " h=" + h + " m=" + m + " s=" + s);
        }
    }

    public class TimeMonitor{
        private int hours = 0;
        private int minutes = 0;
        private int seconds = 0;

        // Något något system.Time - 1000 ms 

        public TimeMonitor(ClockInput in, ClockOutput out){
             
            
        }
    }
}

