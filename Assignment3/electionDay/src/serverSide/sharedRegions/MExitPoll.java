package serverSide.sharedRegions;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import interfaces.ExitPoll.IExitPoll_all;
import interfaces.Logger.ILogger_ExitPoll; // Changed from ILogger_ExitPoll to ILogger_ExitPoll

public class MExitPoll extends UnicastRemoteObject implements IExitPoll_all {
    private static final long serialVersionUID = 1L; // Added serialVersionUID

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


    private ILogger_ExitPoll logger; // Changed from ILogger_ExitPoll to ILogger_ExitPoll
    private int localExitPollPercentage;

    public MExitPoll(ILogger_ExitPoll logger) throws RemoteException { // Changed to ILogger_ExitPoll and made public
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

    public static MExitPoll getInstance(ILogger_ExitPoll logger) throws RemoteException { // Changed to ILogger_ExitPoll
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

                voterReady.signal();
                return;
            }


            if (Math.random() >= 0.6) {
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_DECLINED_TO_ANSWER", "Did not want to answer.");

                voterReady.signal();
                return;
            }


            while (newVoteReady == true) {
                pollsterReady.await();
            }

            if (Math.random() >= 0.2) {
                registeredVote = realVote;
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_TRUTH", "Registered vote: " + (registeredVote ? "A" : "B"));
            } else {
                registeredVote = !realVote;
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_LIE", "Registered vote: " + (registeredVote ? "A" : "B"));

            }
            newVoteReady = true;
            voterReady.signal();
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            if (logger != null) logger.logGeneral("MExitPoll: Interruption in exitPollingStation for voter " + voterId + ": " + e.getMessage());
            throw new RemoteException("Interrupted while waiting in exit polling station", e);

        } finally {
            lock.unlock();
        }
    }

    @Override

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
    }

    @Override

    public void closeIn(int stillVotersInQueue) throws RemoteException {
        lock.lock();
        try {
            if (stillVotersInQueue <= 0) {
                isOpen = false;
                if (logger != null) logger.logGeneral("MExitPoll: Closing immediately, no voters in queue.");
                System.out.println("Exit poll closed (no voters in queue)");
                voterReady.signalAll();
                pollsterReady.signalAll();

                return;
            }
            this.closeIn = stillVotersInQueue;
            this.aboutToClose = true;

            if (logger != null) logger.logGeneral("MExitPoll: About to close, will close in " + stillVotersInQueue + " voters.");

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
                    if (logger != null) logger.logGeneral("MExitPoll: Now closed after countdown.");
                    System.out.println("Exit poll effectively closed after voter countdown.");
                    voterReady.signalAll();
                    pollsterReady.signalAll();
                }
            }
        } finally {
            lock.unlock();

        }
    }

    @Override

    public void printExitPollResults() throws RemoteException {
        if (logger != null) logger.logResults("EXIT_POLL", votesForA, votesForB);
        System.out.println("Exit Poll Results: A - " + votesForA + ", B - " + votesForB);

    }
}
