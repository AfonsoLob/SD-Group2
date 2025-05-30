package serverSide.sharedRegions;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import serverSide.interfaces.Logger.ILogger_PollingStation;
import serverSide.interfaces.PollingStation.IPollingStation_all;

public class MPollingStation extends UnicastRemoteObject implements IPollingStation_all {
    private static final long serialVersionUID = 1L; // Added serialVersionUID

    private static MPollingStation instance;
    private final int capacity;
    private boolean isOpen;
    private transient final Condition stationOpen; // Marked transient
    private ILogger_PollingStation logger;

    // id validation
    private HashSet<Integer> validatedIDs;
    private boolean isAproved;
    private int aprovalId;

    // queue

    private transient final Queue<Integer> votersQueue; // Marked transient
    // queue lock
    private final ReentrantLock queue_lock;
    private transient final Condition notEmpty; // Marked transient
    private transient final Condition notFull; // Marked transient
    private transient final Condition aprovalReady; // Marked transient
    private transient final Condition clerkAprovalReady; // Marked transient

    // voting

    private final ReentrantLock voting_lock;
    private int candidateA;
    private int candidateB;


    private MPollingStation(ILogger_PollingStation logger) throws RemoteException {
        super();
        this.logger = logger;
        int effectiveCapacity = 2;
        try {
            if (this.logger != null) {
                effectiveCapacity = this.logger.getPollingStationCapacity();
                if (effectiveCapacity < 2 || effectiveCapacity > 5) {
                    System.err.println("MPollingStation: Capacity from logger (" + effectiveCapacity + ") out of range [2,5]. Using default 2.");
                    effectiveCapacity = 2;
                }
            } else {
                System.err.println("MPollingStation: Logger is null, using default capacity 2.");
            }
        } catch (RemoteException e) {
            System.err.println("MPollingStation: Failed to get capacity from logger: " + e.getMessage());
        }
        this.capacity = effectiveCapacity;
        this.isOpen = false;


        this.isAproved = false;
        this.aprovalId = -1;


        this.votersQueue = new ArrayDeque<>();
        this.validatedIDs = new HashSet<>();

        this.queue_lock = new ReentrantLock(true);

        this.notEmpty = queue_lock.newCondition();
        this.notFull = queue_lock.newCondition();
        this.stationOpen = queue_lock.newCondition();
        this.aprovalReady = queue_lock.newCondition();
        this.clerkAprovalReady = queue_lock.newCondition();

        this.voting_lock = new ReentrantLock();
        this.candidateA = 0;
        this.candidateB = 0;
    }


    public static MPollingStation getInstance(ILogger_PollingStation logger) throws RemoteException {
        if (instance == null) {
            instance = new MPollingStation(logger);

        }
        return instance;
    }


    private boolean validateID(int voterId) throws RemoteException {

        boolean response = false;
        if (!validatedIDs.contains(voterId)) {
            response = true;
            validatedIDs.add(voterId);
        }
        queue_lock.lock();
        try {
            isAproved = response;
            aprovalId = voterId;
            aprovalReady.signal();
        } finally {
            queue_lock.unlock();
        }

        if (logger != null) logger.logClerkState("VALIDATED_ID", "Voter " + voterId + " validation: " + response);

        return response;
    }

    @Override

    public boolean enterPollingStation(int voterId) throws RemoteException {
        if (logger != null) logger.logVoterState(voterId, "ARRIVED_PS", "At polling station door.");
        queue_lock.lock();
        try {
            while (!isOpen) {
                if (logger != null) logger.logVoterState(voterId, "WAITING_PS_OPEN", "Polling station closed.");
                try {
                    stationOpen.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (logger != null) logger.logGeneral("MPollingStation: Interruption for voter " + voterId + " waiting for station to open: " + e.getMessage());
                    throw new RemoteException("Interrupted while waiting for station to open", e);
                }
            }

            while (votersQueue.size() >= capacity) {
                if (logger != null) logger.logVoterState(voterId, "WAITING_QUEUE_SPACE", "Queue full, capacity: " + capacity);
                try {
                    notFull.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (logger != null) logger.logGeneral("MPollingStation: Interruption for voter " + voterId + " waiting for queue space: " + e.getMessage());
                    throw new RemoteException("Interrupted while waiting for queue to have space", e);
                }
            }

            votersQueue.offer(voterId);
            notEmpty.signal();
            if (logger != null) logger.logVoterState(voterId, "IN_QUEUE", "Entered queue. Size: " + votersQueue.size());
            return true;

        } finally {

            queue_lock.unlock();
        }
    }

    @Override

    public boolean waitIdValidation(int voterId) throws RemoteException {
        if (logger != null) logger.logVoterState(voterId, "WAITING_ID_VALIDATION", "");
        queue_lock.lock();
        try {
            while (aprovalId != voterId) {
                try {
                    aprovalReady.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (logger != null) logger.logGeneral("MPollingStation: Interruption for voter " + voterId + " waiting for ID validation: " + e.getMessage());
                    throw new RemoteException("Interrupted while waiting for ID validation", e);
                }

            }

            aprovalId = -1;
            clerkAprovalReady.signal();

            if (logger != null) logger.logVoterState(voterId, "ID_VALIDATION_RESULT", "Result: " + isAproved);
            return isAproved;

        } finally {

            queue_lock.unlock();
        }
    }

    @Override

    public boolean callNextVoter() throws RemoteException {
        queue_lock.lock();
        try {
            while (votersQueue.isEmpty()) {
                if (logger != null) logger.logClerkState("WAITING_VOTERS", "Queue empty.");
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (logger != null) logger.logGeneral("MPollingStation: Clerk interrupted while waiting for voters: " + e.getMessage());
                    throw new RemoteException("Interrupted while waiting for voters", e);
                }
            }

            if (aprovalId != -1) {
                if (logger != null) logger.logClerkState("WAITING_VALIDATION_CYCLE", "Previous validation pending.");
                try {
                    clerkAprovalReady.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (logger != null) logger.logGeneral("MPollingStation: Clerk interrupted while waiting for validation cycle: " + e.getMessage());
                    throw new RemoteException("Interrupted while waiting for clerk approval readiness", e);
                }
            }

            int id = votersQueue.poll();
            notFull.signal();
            if (logger != null) logger.logClerkState("CALLING_VOTER", "Calling voter " + id);
            return validateID(id);

        } finally {
            queue_lock.unlock();
        }
    }

    @Override

    public void openPollingStation() throws RemoteException {

        queue_lock.lock();
        try {
            isOpen = true;
            stationOpen.signalAll();

            if (logger != null) logger.logGeneral("Polling Station Opened.");
        } finally {

            queue_lock.unlock();
        }
    }

    @Override

    public void closePollingStation() throws RemoteException {
        queue_lock.lock();
        try {
            isOpen = false;
            if (logger != null) logger.logGeneral("Polling Station Closed.");
            stationOpen.signalAll();
            notEmpty.signalAll();
        } finally {

            queue_lock.unlock();
        }
    }

    @Override

    public boolean isOpen() throws RemoteException {

        queue_lock.lock();
        try {
            return isOpen;
        } finally {
            queue_lock.unlock();
        }
    }

    @Override

    public int numberVotersInQueue() throws RemoteException {

        queue_lock.lock();
        try {
            return votersQueue.size();
        } finally {
            queue_lock.unlock();
        }
    }

    @Override

    public void voteA(int voterId) throws RemoteException {
        voting_lock.lock();
        try {
            long waitTime = Math.round((Math.random() * 20 + 30) / 1);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (logger != null) logger.logGeneral("MPollingStation: Voter " + voterId + " interrupted during vote A delay: " + e.getMessage());
                throw new RemoteException("Interrupted during voting delay for A", e);
            }

            candidateA++;
            if (logger != null) logger.logVoterState(voterId, "VOTED_A", "Total A: " + candidateA);
        } finally {

            voting_lock.unlock();
        }
    }

    @Override

    public void voteB(int voterId) throws RemoteException {
        voting_lock.lock();
        try {
            long waitTime = Math.round((Math.random() * 20 + 30) / 1);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (logger != null) logger.logGeneral("MPollingStation: Voter " + voterId + " interrupted during vote B delay: " + e.getMessage());
                throw new RemoteException("Interrupted during voting delay for B", e);
            }

            candidateB++;
            if (logger != null) logger.logVoterState(voterId, "VOTED_B", "Total B: " + candidateB);
        } finally {

            voting_lock.unlock();
        }
    }

    @Override

    public void printFinalResults() throws RemoteException {
        if (logger != null) logger.logResults("POLLING_STATION", candidateA, candidateB);

    }
}
