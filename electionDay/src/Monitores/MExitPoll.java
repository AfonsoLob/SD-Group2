package Monitores;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import Interfaces.IExitPoll;
import Logging.Logger;

public class MExitPoll implements IExitPoll{
    private final int percentage;
    private final ReentrantLock lock;
    private final Condition voterReady;
    private final Condition pollsterReady;
    private boolean theVote;
    private boolean newVoteReady;

    private boolean isOpen;
    private boolean aboutToClose;
    private int closeIn;

    // private int countLies;
    // private int countTruths;

    private Logger logger;

    private MExitPoll(int percentage, Logger logger) {
        this.percentage = percentage;
        this.lock = new ReentrantLock();
        this.voterReady = lock.newCondition();
        this.logger = logger;
        this.pollsterReady = lock.newCondition();
        this.isOpen = true;
        this.aboutToClose = false;
    }

    public static MExitPoll getInstance(int percentage, Logger logger) {
        return new MExitPoll(percentage, logger);
    }

    @Override
    public void exitPollingStation(int voterId, boolean myVote) { 
        
        tryClosingExitPoll();
        
        try{

            lock.lock();

            if (Math.random() * 100 >= percentage) { // percentage chance of being selected
                System.out.println("Voter " + voterId + " leaving polling station (not selected for questioning)");
                logger.voterExiting(voterId, false);
                voterReady.signal();
                return;
            }

            if (Math.random() >= 0.6) { // 40% don't want to answer
                System.out.println("Voter " + voterId + " leaving polling station (doesn't want to answer)");
                logger.voterExiting(voterId, false);
                voterReady.signal();
                return;
            }

            while(newVoteReady == true){
                pollsterReady.await();
            }

            if (Math.random()>= 0.2){ // 80% tell the truth
                theVote = myVote;
                System.out.println("Voter " + voterId + " leaving polling station (telling the truth)");
                logger.voterExiting(voterId, true);
            } else { // rest lie
                theVote = !myVote;
                System.out.println("Voter " + voterId + " leaving polling station (lying)");
                logger.voterExiting(voterId, true);
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
    public int inquire(){
        lock.lock();
        try {
            while (newVoteReady == false && isOpen) {
                // System.out.println("Pollster waiting...");
                voterReady.await();
            }
            newVoteReady = false;
            pollsterReady.signal();
            
            if (!isOpen) {
                return 0;
            } else if (theVote){
                return 1;
            } else {
                return -1;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0; 
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

    @Override
    public void tryClosingExitPoll(){
        if(aboutToClose){
            closeIn--;
            // System.out.println("Closing in " + closeIn);
            if(closeIn <= 0){isOpen = false;} 
        }
    }
}
