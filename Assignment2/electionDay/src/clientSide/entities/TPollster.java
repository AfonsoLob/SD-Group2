package clientSide.entities;
import clientSide.interfaces.ExitPoll.IExitPoll_Pollster;
// import serverSide.GUI.Gui;

public class TPollster implements Runnable {
    private final IExitPoll_Pollster exitPoll;
    // private final IGUI_Common gui;

    private TPollster(IExitPoll_Pollster exitPoll) {
        this.exitPoll = exitPoll;
        // this.gui = Gui.getInstance();
    }

    public static TPollster getInstance(IExitPoll_Pollster exitPoll) {
        return new TPollster(exitPoll);
    }

    @Override
    public void run() {
        System.out.println("Pollster running");
        do {
            try {
                exitPoll.inquire();
                
                // Apply speed factor - slower speed = longer wait time
                // Increase base wait time for pollster
                // float speedFactor = gui.getSimulationSpeed();
                long waitTime = Math.round((Math.random() * 10 + 15) / 1); // Increased from 5+5 to 10+15
                // long waitTime = Math.round((Math.random() * 10 + 15) / speedFactor);
                Thread.sleep(waitTime);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(exitPoll.isOpen());

        exitPoll.printExitPollResults();

        System.out.println("Pollster terminated");
    }
}
