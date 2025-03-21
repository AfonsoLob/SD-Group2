package Threads;
import Interfaces.IExitPoll;


public class TPollster implements Runnable {
    private final IExitPoll exitPoll;
    // private final IPollingStation pollingStation;

    private TPollster(IExitPoll exitPoll) {
        this.exitPoll = exitPoll;
    }

    public static TPollster getInstance(IExitPoll exitPoll) {
        return new TPollster(exitPoll);
    }

    @Override
    public void run() {
        System.out.println("Pollster running");
        do {
            try {
                exitPoll.inquire();
                Thread.sleep((long) (Math.random() * 5) + 5); // 5-10 ms
                // exitPoll.tryClosingExitPoll();
            }  catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(exitPoll.isOpen());

        exitPoll.printExitPollResults();

        System.out.println("Pollster terminated");
    }
}
