package Monitores;


import java.util.concurrent.locks.Condition;
// import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Interfaces.IPollingStation;
// import Threads.TVoter;

import java.util.ArrayDeque;
import java.util.Queue;

import Logging.Logger;

public class MPollingStation implements IPollingStation {

    private static MPollingStation instance;
    private final int capacity;
    private boolean isOpen;
  
    private boolean isAproved;
    private int aprovalId;

    
    private final Logger logger;

    // queue
    private final Queue<Integer> votersQueue;
    // queue lock
    private final ReentrantLock queue_lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private final Condition aprovalReady;
    // station lock
    private final Condition stationOpen;

    //voting 
    private final ReentrantLock voting_lock;
    private int candidateA;
    private int candidateB;

    private MPollingStation(int capacity, Logger logger) {
        this.logger = logger;
        this.capacity = capacity;
        this.isOpen = false;

        this.isAproved = false;
        this.aprovalId = -1;

        this.votersQueue = new ArrayDeque<Integer>();

        this.queue_lock = new ReentrantLock(true); // true = fair
        this.notEmpty = queue_lock.newCondition();
        this.notFull = queue_lock.newCondition();
        this.stationOpen = queue_lock.newCondition();
        this.aprovalReady = queue_lock.newCondition();


        this.voting_lock = new ReentrantLock();
        this.candidateA = 0;
        this.candidateB = 0;
    }

    public static MPollingStation getInstance(int maxCapacity, Logger logger) {
        if (instance == null) {
            instance = new MPollingStation(maxCapacity, logger);
        }
        return instance;
    }

    @Override
    public boolean enterPollingStation(int voterId) {
        queue_lock.lock();
        logger.voterAtDoor(voterId);
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
            logger.voterEnteringQueue(voterId);
            queue_lock.unlock();
        }
    }

    // private boolean isIdInQueue(int voterId) {
    //     return votersQueue.contains(voterId);
    // }

    @Override
    public boolean waitIdValidation(int voterId) {
        queue_lock.lock();
        try {
            while (aprovalId != voterId) {
                aprovalReady.await();
            }

            aprovalId = -1;
            return isAproved;

        } catch (InterruptedException e) {
            return false;

        } finally {
            logger.validatingVoter(voterId, isAproved);
            queue_lock.unlock();
        }
    }

    @Override
    public int callNextVoter() {
        queue_lock.lock();
        try {
            if (votersQueue.isEmpty()) {
                notEmpty.await();
            }
            int id = votersQueue.poll();
            notFull.signal();
            return id;
        } catch (InterruptedException e) {
            return -1;
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
    public void sendSignal(int voterId, boolean response) {
        queue_lock.lock();
        try {
            isAproved = response;
            aprovalId = voterId;
            aprovalReady.signal();
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
            logger.stationOpening();
            queue_lock.unlock();
        }
    }

    @Override
    public void closePollingStation() {
        queue_lock.lock();
        try {
            isOpen = false;
        } finally {
            logger.stationClosing();
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
            Thread.sleep((long) (Math.random() * 15)); // 0-15 ms
            candidateA++;
            System.out.println("A total votes: " + candidateA);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            logger.voterInBooth(voterId, true); // vote A
            voting_lock.unlock();
        }
        
    }

    @Override
    public void voteB(int voterId) {
        voting_lock.lock();
        try {
            Thread.sleep((long) (Math.random() * 15)); // 0-15 ms
            candidateB++;
            System.out.println("B total votes: " + candidateB);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            logger.voterInBooth(voterId, false); // vote B
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
