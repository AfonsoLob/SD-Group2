package serverSide.sharedRegions;

import serverSide.GUI.Gui;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import clientSide.interfaces.ExitPoll.IExitPoll_all;
import clientSide.interfaces.GUI.IGUI_Common;
import clientSide.interfaces.Logger.ILogger_ExitPoll;

public class MExitPoll implements IExitPoll_all {
    private final int percentage;
    private final ReentrantLock lock;
    private final Condition voterReady;
    private final Condition pollsterReady;
    private boolean registeredVote;
    private boolean newVoteReady;

    private boolean isOpen;
    private boolean aboutToClose;
    private int closeIn;

    private int votesForA;
    private int votesForB;

    // private int countLies;
    // private int countTruths;

    private ILogger_ExitPoll logger;
    private final IGUI_Common gui;

    private MExitPoll(int percentage, ILogger_ExitPoll logger) {
        this.percentage = percentage;
        this.lock = new ReentrantLock();
        this.voterReady = lock.newCondition();
        this.logger = logger;
        this.pollsterReady = lock.newCondition();
        this.isOpen = true;
        this.aboutToClose = false;
        this.gui = Gui.getInstance();

        this.votesForA = 0;
        this.votesForB = 0;
    }

    public static MExitPoll getInstance(int percentage, ILogger_ExitPoll logger) {
        return new MExitPoll(percentage, logger);
    }

    @Override
    public void exitPollingStation(int voterId, boolean realVote, boolean response) { 
        
        tryClosingExitPoll();

        if(!response){
            logger.exitPollVote(voterId, "");
            return;
        }
        
        try{
            lock.lock();

            if (Math.random() * 100 >= percentage) { // percentage chance of being selected
                System.out.println("Voter " + voterId + " leaving polling station (not selected for questioning)");
                logger.exitPollVote(voterId, "");
                voterReady.signal();
                return;
            }

            if (Math.random() >= 0.6) { // 40% don't want to answer
                System.out.println("Voter " + voterId + " leaving polling station (doesn't want to answer)");
                logger.exitPollVote(voterId, "");
                voterReady.signal();
                return;
            }

            while(newVoteReady == true){
                pollsterReady.await();
            }

            if (Math.random()>= 0.2){ // 80% tell the truth
                registeredVote = realVote;
                System.out.println("Voter " + voterId + " leaving polling station (telling the truth)");
                logger.exitPollVote(voterId, registeredVote ? "A" : "B");
            } else { // rest lie
                registeredVote = !realVote;
                System.out.println("Voter " + voterId + " leaving polling station (lying)");
                logger.exitPollVote(voterId, registeredVote ? "A" : "B");
            }
            newVoteReady = true;
            voterReady.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void inquire(){
        lock.lock();
        try {
            while (newVoteReady == false && isOpen) {
                voterReady.await();
            }
            
            if(newVoteReady){
                newVoteReady = false;
                pollsterReady.signal();
                if(registeredVote){
                    votesForA++;
                    System.out.println("Pollster registered one more vote for A");
                }
                else{
                    votesForB++;
                    System.out.println("Pollster registered one more vote for B");
                }
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isOpen() {
        lock.lock();
        try {
            return isOpen;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void closeIn(int stillVotersInQueue){
        lock.lock();
        try{
            if(stillVotersInQueue <= 0){
                isOpen = false;
                voterReady.signal();
                return;
            }
            this.closeIn = stillVotersInQueue;
            this.aboutToClose = true;
        } finally {
            lock.unlock();
        }
    }

    private void tryClosingExitPoll(){
        if(aboutToClose){
            closeIn--;
            if(closeIn <= 0){
                lock.lock();
                try{
                    isOpen = false;
                    voterReady.signal();
                }finally{
                    lock.unlock();
                }
            } 
        }
    }

    @Override
    public void printExitPollResults(){
        int totalVotes = votesForA + votesForB;
        if (totalVotes > 0) {
            double percentA = ((double)votesForA / totalVotes) * 100;
            double percentB = ((double)votesForB / totalVotes) * 100;
            System.out.println("Prediction for A: " + (int)percentA + " percent of the votes");
            System.out.println("Prediction for B: " + (int)percentB + " percent of the votes");
        } else {
            System.out.println("No votes were recorded in the exit poll");
        }
    }
}
