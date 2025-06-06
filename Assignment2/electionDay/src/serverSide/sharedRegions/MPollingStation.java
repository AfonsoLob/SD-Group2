package serverSide.sharedRegions;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import serverSide.interfaces.Pollingstation.IPollingStation_all;

public class MPollingStation implements IPollingStation_all {

    private static IPollingStation_all instance;
    private final int capacity;
    private boolean isOpen;
    private final Condition stationOpen;
    // private final IGUI_Common gui;
  
    //id validation
    private HashSet<Integer> validatedIDs;
    private boolean isAproved;
    private int aprovalId;

    // queue
    private final Queue<Integer> votersQueue;
    // queue lock
    private final ReentrantLock queue_lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private final Condition aprovalReady;
    private final Condition clerkAprovalReady;

    //voting 
    private final ReentrantLock voting_lock;
    private int candidateA;
    private int candidateB;

    private MPollingStation(int capacity) {
        if (capacity < 2 || capacity > 5) {
            throw new IllegalArgumentException("Queue capacity must be between 2 and 5");
        }
        
        this.capacity = capacity;
        this.isOpen = false;
        // this.gui = Gui.getInstance();

        this.isAproved = false;
        this.aprovalId = -1;

        this.votersQueue = new ArrayDeque<Integer>();
        this.validatedIDs = new HashSet<>();

        this.queue_lock = new ReentrantLock(true); // true = fair
        this.notEmpty = queue_lock.newCondition();
        this.notFull = queue_lock.newCondition();
        this.stationOpen = queue_lock.newCondition();
        this.aprovalReady = queue_lock.newCondition();
        this.clerkAprovalReady = queue_lock.newCondition();

        this.voting_lock = new ReentrantLock();
        this.candidateA = 0;
        this.candidateB = 0;
    }

    public static IPollingStation_all getInstance(int maxCapacity) {
        if (instance == null) {
            instance = new MPollingStation(maxCapacity);
        }
        return instance;
    }

    private boolean validateID(int voterId) {
        // check if voterid is in hashset, if not add it and mark it as positive, and if yes mark it as negative
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

        return response;
    }

    @Override
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
            queue_lock.unlock();
        }
    }

    @Override
    public boolean waitIdValidation(int voterId) {
        queue_lock.lock();
        try {
            while (aprovalId != voterId) {
                aprovalReady.await();
            }

            aprovalId = -1;
            clerkAprovalReady.signal();
            return isAproved;

        } catch (InterruptedException e) {
            return false;

        } finally {
            // logger.validatingVoter(voterId, isAproved);
            queue_lock.unlock();
        }
    }

    @Override
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
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
    public void openPollingStation() {
        queue_lock.lock();
        try {
            isOpen = true;
            stationOpen.signalAll();
        } finally {
            // logger.stationOpening();
            queue_lock.unlock();
        }
    }

    @Override
    public void closePollingStation() {
        queue_lock.lock();
        try {
            isOpen = false;
        System.out.println("IM CLOSED DO NOT ENTER");
        } finally {
            // logger.stationClosing();
            queue_lock.unlock();
        }
    }

    @Override
    public boolean isOpen() {
        queue_lock.lock();
        try {
            return isOpen;
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
    public int numberVotersInQueue() {
        queue_lock.lock();
        try {
            return votersQueue.size();
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
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
            logger.voterInBooth(voterId, true); // vote A - FIXED: Uncommented
            voting_lock.unlock();
        }
    }

    @Override
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
            voting_lock.unlock();
        }
    }

    @Override
    public void printFinalResults() {
        System.out.println("Final results: ");
        System.out.println("Candidate A: " + candidateA);
        System.out.println("Candidate B: " + candidateB);  
    }
}
