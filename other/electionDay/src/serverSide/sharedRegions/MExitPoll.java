package serverSide.sharedRegions;

<<<<<<< HEAD
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import serverSide.interfaces.ExitPoll.IExitPoll_all;
import serverSide.interfaces.Logger.ILogger_all; // Changed from ILogger_ExitPoll to ILogger_all

public class MExitPoll extends UnicastRemoteObject implements IExitPoll_all {
    private static final long serialVersionUID = 1L; // Added serialVersionUID

    private final ReentrantLock lock;
    private transient final Condition voterReady; // Marked transient
    private transient final Condition pollsterReady; // Marked transient
=======
// import clientSide.interfaces.ExitPoll.IExitPoll_all;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import serverSide.interfaces.ExitPoll.IExitPoll_all;

public class MExitPoll implements IExitPoll_all {
    private final int percentage;
    private final ReentrantLock lock;
    private final Condition voterReady;
    private final Condition pollsterReady;
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
    private boolean registeredVote;
    private boolean newVoteReady;

    private boolean isOpen;
    private boolean aboutToClose;
    private int closeIn;

    private int votesForA;
    private int votesForB;

<<<<<<< HEAD
    private ILogger_all logger; // Changed from ILogger_ExitPoll to ILogger_all
    private int localExitPollPercentage;

    public MExitPoll(ILogger_all logger) throws RemoteException { // Changed to ILogger_all and made public
        super();
        this.logger = logger;
        this.lock = new ReentrantLock();
        this.voterReady = lock.newCondition();
        this.pollsterReady = lock.newCondition();
        this.isOpen = true;
        this.aboutToClose = false;
        this.votesForA = 0;
        this.votesForB = 0;

        try {
            if (this.logger != null) {
                this.localExitPollPercentage = this.logger.getExitPollPercentage();
            } else {
                this.localExitPollPercentage = 20;
                System.err.println("MExitPoll: Logger is null, using default exit poll percentage.");
            }
        } catch (RemoteException e) {
            System.err.println("MExitPoll: Failed to get exit poll percentage from logger: " + e.getMessage());
            this.localExitPollPercentage = 20;
        }
    }

    public static MExitPoll getInstance(ILogger_all logger) throws RemoteException { // Changed to ILogger_all
        return new MExitPoll(logger);
    }

    @Override
    public void exitPollingStation(int voterId, boolean realVote, boolean response) throws RemoteException {
        tryClosingExitPoll();

        if (!response) {
            if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_SKIPPED", "Declined to participate or not selected initially.");
            return;
        }

        try {
            lock.lock();

            if (Math.random() * 100 >= this.localExitPollPercentage) {
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_NOT_SELECTED_CHANCE", "Percentage chance not met.");
=======
    // private int countLies;
    // private int countTruths;

    // private final ILogger_ExitPoll logger;


    private MExitPoll(int percentage) {
        this.percentage = percentage;
        this.lock = new ReentrantLock();
        this.voterReady = lock.newCondition();
        // this.logger = logger;
        this.pollsterReady = lock.newCondition();
        this.isOpen = true;
        this.aboutToClose = false;
        

        this.votesForA = 0;
        this.votesForB = 0;
    }

    public static MExitPoll getInstance(int percentage) {
        return new MExitPoll(percentage);
    }

    @Override
    public void exitPollingStation(int voterId, boolean realVote, boolean response) { 
        
        tryClosingExitPoll();

        if(!response){
            // logger.exitPollVote(voterId, "");
            return;
        }
        
        try{
            lock.lock();

            if (Math.random() * 100 >= percentage) { // percentage chance of being selected
                System.out.println("Voter " + voterId + " leaving polling station (not selected for questioning)");
                // logger.exitPollVote(voterId, "");
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
                voterReady.signal();
                return;
            }

<<<<<<< HEAD
            if (Math.random() >= 0.6) {
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_DECLINED_TO_ANSWER", "Did not want to answer.");
=======
            if (Math.random() >= 0.6) { // 40% don't want to answer
                System.out.println("Voter " + voterId + " leaving polling station (doesn't want to answer)");
                // logger.exitPollVote(voterId, "");
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
                voterReady.signal();
                return;
            }

<<<<<<< HEAD
            while (newVoteReady == true) {
                pollsterReady.await();
            }

            if (Math.random() >= 0.2) {
                registeredVote = realVote;
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_TRUTH", "Registered vote: " + (registeredVote ? "A" : "B"));
            } else {
                registeredVote = !realVote;
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_LIE", "Registered vote: " + (registeredVote ? "A" : "B"));
=======
            while(newVoteReady == true){
                pollsterReady.await();
            }

            if (Math.random()>= 0.2){ // 80% tell the truth
                registeredVote = realVote;
                System.out.println("Voter " + voterId + " leaving polling station (telling the truth)");
                // logger.exitPollVote(voterId, registeredVote ? "A" : "B");
            } else { // rest lie
                registeredVote = !realVote;
                System.out.println("Voter " + voterId + " leaving polling station (lying)");
                // logger.exitPollVote(voterId, registeredVote ? "A" : "B");
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            }
            newVoteReady = true;
            voterReady.signal();
        } catch (InterruptedException e) {
<<<<<<< HEAD
            Thread.currentThread().interrupt();
            if (logger != null) logger.logGeneral("MExitPoll: Interruption in exitPollingStation for voter " + voterId + ": " + e.getMessage());
            throw new RemoteException("Interrupted while waiting in exit polling station", e);
=======
            e.printStackTrace();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        } finally {
            lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public void inquire() throws RemoteException {
        lock.lock();
        try {
            if (logger != null) logger.logPollsterState("INQUIRE_WAITING", "Waiting for new vote.");
            while (newVoteReady == false && isOpen) {
                voterReady.await();
            }

            if (newVoteReady) {
                newVoteReady = false;
                pollsterReady.signal();
                if (registeredVote) {
                    votesForA++;
                    if (logger != null) logger.logPollsterState("INQUIRE_VOTE_A", "Votes A: " + votesForA);
                } else {
                    votesForB++;
                    if (logger != null) logger.logPollsterState("INQUIRE_VOTE_B", "Votes B: " + votesForB);
                }
            } else if (!isOpen) {
                if (logger != null) logger.logPollsterState("INQUIRE_CLOSED", "Exit poll closed while waiting.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (logger != null) logger.logGeneral("MExitPoll: Interruption in inquire: " + e.getMessage());
            throw new RemoteException("Interrupted while waiting to inquire", e);
=======
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
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        } finally {
            lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public boolean isOpen() throws RemoteException {
=======
    public boolean isOpen() {
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        lock.lock();
        try {
            return isOpen;
        } finally {
            lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public void closeIn(int stillVotersInQueue) throws RemoteException {
        lock.lock();
        try {
            if (stillVotersInQueue <= 0) {
                isOpen = false;
                if (logger != null) logger.logGeneral("MExitPoll: Closing immediately, no voters in queue.");
                System.out.println("Exit poll closed (no voters in queue)");
                voterReady.signalAll();
                pollsterReady.signalAll();
=======
    public void closeIn(int stillVotersInQueue){
        lock.lock();
        try{
            if(stillVotersInQueue <= 0){
                isOpen = false;
                System.out.println("Exit poll closed (no voters in queue)");
                voterReady.signal();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
                return;
            }
            this.closeIn = stillVotersInQueue;
            this.aboutToClose = true;
<<<<<<< HEAD
            if (logger != null) logger.logGeneral("MExitPoll: About to close, will close in " + stillVotersInQueue + " voters.");
=======
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        } finally {
            lock.unlock();
        }
    }

<<<<<<< HEAD
    private void tryClosingExitPoll() throws RemoteException {
        lock.lock();
        try {
            if (aboutToClose) {
                closeIn--;
                if (closeIn <= 0) {
                    isOpen = false;
                    aboutToClose = false;
                    if (logger != null) logger.logGeneral("MExitPoll: Now closed after countdown.");
                    System.out.println("Exit poll effectively closed after voter countdown.");
                    voterReady.signalAll();
                    pollsterReady.signalAll();
                }
            }
        } finally {
            lock.unlock();
=======
    private void tryClosingExitPoll(){
        if(aboutToClose){
            closeIn--;
            if(closeIn <= 0){
                lock.lock();
                try{
                    isOpen = false;
                    System.out.println("Exit poll closed (no voters in queue)");
                    voterReady.signal();
                }finally{
                    lock.unlock();
                }
            } 
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        }
    }

    @Override
<<<<<<< HEAD
    public void printExitPollResults() throws RemoteException {
        if (logger != null) logger.logResults("EXIT_POLL", votesForA, votesForB);
        System.out.println("Exit Poll Results: A - " + votesForA + ", B - " + votesForB);
=======
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
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
    }
}
