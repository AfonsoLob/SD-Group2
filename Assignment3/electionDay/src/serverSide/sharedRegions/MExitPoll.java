package serverSide.sharedRegions;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import serverSide.interfaces.ExitPoll.IExitPoll_all;
import serverSide.interfaces.Logger.ILogger_ExitPoll; // Changed from ILogger_ExitPoll to ILogger_ExitPoll

public class MExitPoll extends UnicastRemoteObject implements IExitPoll_all {
    private static final long serialVersionUID = 1L; // Added serialVersionUID
    private static MExitPoll instance = null; // Singleton instance

    private final ReentrantLock lock;
    private transient final Condition voterReady; // Marked transient
    private transient final Condition pollsterReady; // Marked transient

    private boolean registeredVote;
    private boolean newVoteReady;

    private boolean isOpen;
    private boolean aboutToClose;
    private int closeIn;

    private int votesForA;
    private int votesForB;


    private transient ILogger_ExitPoll logger; // Changed from ILogger_ExitPoll to ILogger_ExitPoll (transient as interface cannot be serialized)
    private int localExitPollPercentage;

    private MExitPoll(ILogger_ExitPoll logger) throws RemoteException { // Changed to ILogger_ExitPoll and made private
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

    public static synchronized MExitPoll getInstance(ILogger_ExitPoll logger) throws RemoteException { // Changed to ILogger_ExitPoll
        if (instance == null) {
            instance = new MExitPoll(logger);
        }
        return instance;
    }

    @Override
    public void exitPollingStation(int voterId, boolean realVote, boolean response) throws RemoteException {
        tryClosingExitPoll();

        if (!response) {
            // Log exit poll vote with empty string for declined participation
            if (logger != null) logger.exitPollVote(voterId, "");
            return;
        }

        try {
            lock.lock();

            if (Math.random() * 100 >= this.localExitPollPercentage) {
                // Not selected by percentage chance
                if (logger != null) logger.exitPollVote(voterId, "");
                voterReady.signal();
                return;
            }

            if (Math.random() >= 0.6) {
                // Declined to answer (40% don't want to answer)
                if (logger != null) logger.exitPollVote(voterId, "");
                voterReady.signal();
                return;
            }

            while (newVoteReady == true) {
                pollsterReady.await();
            }

            if (Math.random() >= 0.2) {
                // 80% tell the truth
                registeredVote = realVote;
                System.out.println("Voter " + voterId + " leaving polling station (telling the truth)");
                if (logger != null) logger.exitPollVote(voterId, registeredVote ? "A" : "B");
            } else {
                // 20% lie
                registeredVote = !realVote;
                System.out.println("Voter " + voterId + " leaving polling station (lying)");
                if (logger != null) logger.exitPollVote(voterId, registeredVote ? "A" : "B");
            }
            newVoteReady = true;
            voterReady.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting in exit polling station", e);
        } finally {
            lock.unlock();
        }
    }    @Override
    public void inquire() throws RemoteException {
        lock.lock();
        try {
            while (newVoteReady == false && isOpen) {
                voterReady.await();
            }

            if (newVoteReady) {
                newVoteReady = false;
                pollsterReady.signal();
                if (registeredVote) {
                    votesForA++;
                    System.out.println("Pollster registered one more vote for A");
                } else {
                    votesForB++;
                    System.out.println("Pollster registered one more vote for B");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting to inquire", e);
        } finally {
            lock.unlock();
        }
    }

    @Override

    public boolean isOpen() throws RemoteException {

        lock.lock();
        try {
            return isOpen;
        } finally {
            lock.unlock();
        }
    }    @Override
    public void closeIn(int stillVotersInQueue) throws RemoteException {
        lock.lock();
        try {
            if (stillVotersInQueue <= 0) {
                isOpen = false;
                System.out.println("Exit poll closed (no voters in queue)");
                voterReady.signalAll();
                pollsterReady.signalAll();
                return;
            }
            this.closeIn = stillVotersInQueue;
            this.aboutToClose = true;
        } finally {
            lock.unlock();
        }
    }


    private void tryClosingExitPoll() throws RemoteException {
        lock.lock();
        try {
            if (aboutToClose) {
                closeIn--;
                if (closeIn <= 0) {
                    isOpen = false;
                    aboutToClose = false;
                    System.out.println("Exit poll effectively closed after voter countdown.");
                    voterReady.signalAll();
                    pollsterReady.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }    @Override
    public void printExitPollResults() throws RemoteException {
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
