package serverSide.sharedRegions;

<<<<<<< HEAD
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
=======
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

<<<<<<< HEAD
import serverSide.interfaces.Logger.ILogger_all;
import serverSide.interfaces.PollingStation.IPollingStation_all;

public class MPollingStation extends UnicastRemoteObject implements IPollingStation_all {
    private static final long serialVersionUID = 1L; // Added serialVersionUID

    private static MPollingStation instance;
    private final int capacity;
    private boolean isOpen;
    private transient final Condition stationOpen; // Marked transient
    private ILogger_all logger;

    // id validation
=======
import serverSide.interfaces.Pollingstation.IPollingStation_all;

public class MPollingStation implements IPollingStation_all {

    private static IPollingStation_all instance;
    private final int capacity;
    private boolean isOpen;
    private final Condition stationOpen;
    // private final IGUI_Common gui;
  
    //id validation
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
    private HashSet<Integer> validatedIDs;
    private boolean isAproved;
    private int aprovalId;

    // queue
<<<<<<< HEAD
    private transient final Queue<Integer> votersQueue; // Marked transient
    // queue lock
    private final ReentrantLock queue_lock;
    private transient final Condition notEmpty; // Marked transient
    private transient final Condition notFull; // Marked transient
    private transient final Condition aprovalReady; // Marked transient
    private transient final Condition clerkAprovalReady; // Marked transient

    // voting
=======
    private final Queue<Integer> votersQueue;
    // queue lock
    private final ReentrantLock queue_lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private final Condition aprovalReady;
    private final Condition clerkAprovalReady;

    //voting 
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
    private final ReentrantLock voting_lock;
    private int candidateA;
    private int candidateB;

<<<<<<< HEAD
    private MPollingStation(ILogger logger) throws RemoteException {
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
=======
    private MPollingStation(int capacity) {
        if (capacity < 2 || capacity > 5) {
            throw new IllegalArgumentException("Queue capacity must be between 2 and 5");
        }
        
        this.capacity = capacity;
        this.isOpen = false;
        // this.gui = Gui.getInstance();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87

        this.isAproved = false;
        this.aprovalId = -1;

<<<<<<< HEAD
        this.votersQueue = new ArrayDeque<>();
        this.validatedIDs = new HashSet<>();

        this.queue_lock = new ReentrantLock(true);
=======
        this.votersQueue = new ArrayDeque<Integer>();
        this.validatedIDs = new HashSet<>();

        this.queue_lock = new ReentrantLock(true); // true = fair
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        this.notEmpty = queue_lock.newCondition();
        this.notFull = queue_lock.newCondition();
        this.stationOpen = queue_lock.newCondition();
        this.aprovalReady = queue_lock.newCondition();
        this.clerkAprovalReady = queue_lock.newCondition();

        this.voting_lock = new ReentrantLock();
        this.candidateA = 0;
        this.candidateB = 0;
    }

<<<<<<< HEAD
    public static MPollingStation getInstance(ILogger logger) throws RemoteException {
        if (instance == null) {
            instance = new MPollingStation(logger);
=======
    public static IPollingStation_all getInstance(int maxCapacity) {
        if (instance == null) {
            instance = new MPollingStation(maxCapacity);
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        }
        return instance;
    }

<<<<<<< HEAD
    private boolean validateID(int voterId) throws RemoteException {
=======
    private boolean validateID(int voterId) {
        // check if voterid is in hashset, if not add it and mark it as positive, and if yes mark it as negative
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
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
<<<<<<< HEAD
        if (logger != null) logger.logClerkState("VALIDATED_ID", "Voter " + voterId + " validation: " + response);
=======

>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        return response;
    }

    @Override
<<<<<<< HEAD
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
=======
    public boolean enterPollingStation(int voterId) {
        queue_lock.lock();
        // logger.voterAtDoor(voterId);
        try {
            while (!isOpen) {
                stationOpen.await();
            }
            
            while (votersQueue.size() >= capacity) {
                notFull.await();
            }
            
            votersQueue.offer(voterId);
            notEmpty.signal();
            return true;
            
        } catch (InterruptedException e) {
            return false;
        } finally {
            // logger.voterEnteringQueue(voterId);
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            queue_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
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
=======
    public boolean waitIdValidation(int voterId) {
        queue_lock.lock();
        try {
            while (aprovalId != voterId) {
                aprovalReady.await();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            }

            aprovalId = -1;
            clerkAprovalReady.signal();
<<<<<<< HEAD
            if (logger != null) logger.logVoterState(voterId, "ID_VALIDATION_RESULT", "Result: " + isAproved);
            return isAproved;

        } finally {
=======
            return isAproved;

        } catch (InterruptedException e) {
            return false;

        } finally {
            // logger.validatingVoter(voterId, isAproved);
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            queue_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
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
=======
    public boolean callNextVoter() {
        queue_lock.lock();
        try {
            if (votersQueue.isEmpty()) {
                System.out.println("Waiting for voters");
                notEmpty.await();
            }

            if(aprovalId != -1) {
                clerkAprovalReady.await();
            }

            int id = votersQueue.poll();
            System.out.println("Voter " + id + " is called");
            notFull.signal();
            return validateID(id);
        } catch (InterruptedException e) {
            System.out.println("Error while waiting for voters");
            return false;
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public void openPollingStation() throws RemoteException {
=======
    public void openPollingStation() {
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        queue_lock.lock();
        try {
            isOpen = true;
            stationOpen.signalAll();
<<<<<<< HEAD
            if (logger != null) logger.logGeneral("Polling Station Opened.");
        } finally {
=======
        } finally {
            // logger.stationOpening();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            queue_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public void closePollingStation() throws RemoteException {
        queue_lock.lock();
        try {
            isOpen = false;
            if (logger != null) logger.logGeneral("Polling Station Closed.");
            stationOpen.signalAll();
            notEmpty.signalAll();
        } finally {
=======
    public void closePollingStation() {
        queue_lock.lock();
        try {
            isOpen = false;
        System.out.println("IM CLOSED DO NOT ENTER");
        } finally {
            // logger.stationClosing();
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            queue_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public boolean isOpen() throws RemoteException {
=======
    public boolean isOpen() {
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        queue_lock.lock();
        try {
            return isOpen;
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public int numberVotersInQueue() throws RemoteException {
=======
    public int numberVotersInQueue() {
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        queue_lock.lock();
        try {
            return votersQueue.size();
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
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
=======
    public void voteA(int voterId) {
        voting_lock.lock();
        try {
            // Apply speed factor for voting time - increase minimum time required to vote
            // float speedFactor = gui.getSimulationSpeed();
            long waitTime = Math.round((Math.random() * 20 + 30) / 1);  // 30-50ms instead of 0-15ms
            Thread.sleep(waitTime);
            
            candidateA++;
            System.out.println("A total votes: " + candidateA);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // logger.voterInBooth(voterId, true); // vote A
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            voting_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
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
=======
    public void voteB(int voterId) {
        voting_lock.lock();
        try {
            // Apply speed factor for voting time - increase minimum time required to vote
            // float speedFactor = gui.getSimulationSpeed();
            long waitTime = Math.round((Math.random() * 20 + 30) / 1);  // 30-50ms instead of 0-15ms  
            Thread.sleep(waitTime);
            
            candidateB++;
            System.out.println("B total votes: " + candidateB);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // logger.voterInBooth(voterId, false); // vote B
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
            voting_lock.unlock();
        }
    }

    @Override
<<<<<<< HEAD
    public void printFinalResults() throws RemoteException {
        if (logger != null) logger.logResults("POLLING_STATION", candidateA, candidateB);
=======
    public void printFinalResults() {
        System.out.println("Final results: ");
        System.out.println("Candidate A: " + candidateA);
        System.out.println("Candidate B: " + candidateB);  
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
    }
}
