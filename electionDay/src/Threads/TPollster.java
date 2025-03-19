package Threads;
import Interfaces.IExitPoll;
import Logging.Logger;


public class TPollster implements Runnable {
    private int votesForA;
    private int votesForB;
    private final IExitPoll exitPoll;
    // private final IPollingStation pollingStation;

    private TPollster(IExitPoll exitPoll) {
        this.votesForA = 0;
        this.votesForB = 0;
        this.exitPoll = exitPoll;
        // this.pollingStation = pollingStation;
    }

    public static TPollster getInstance(IExitPoll exitPoll) {
        return new TPollster(exitPoll);
    }

    // private static TPollster getInstance(IExitPoll exitPoll) {
    //     return new TPollster(exitPoll);
    // }

    @Override
    public void run() {
        System.out.println("Pollster running");
        // loger do something 
        int query;
        do {
            try {
                query = exitPoll.inquire();
                if(query == 1){
                    votesForA++;
                    System.out.println("Pollster registered one more vote for A");
                }
                else if (query == -1){
                    votesForB++;
                    System.out.println("Pollster registered one more vote for B");
                }
                Thread.sleep((long) (Math.random() * 5) + 5); // 5-10 ms
                // exitPoll.tryClosingExitPoll();
            }  catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(exitPoll.isOpen());

        int totalVotes = votesForA + votesForB;
        if (totalVotes > 0) {
            double percentA = ((double)votesForA / totalVotes) * 100;
            double percentB = ((double)votesForB / totalVotes) * 100;
            System.out.println("Prediction for A: " + (int)percentA + " percent of the votes");
            System.out.println("Prediction for B: " + (int)percentB + " percent of the votes");
        } else {
            System.out.println("No votes were recorded in the exit poll");
        }

        System.out.println("Pollster terminated");
    }
}
