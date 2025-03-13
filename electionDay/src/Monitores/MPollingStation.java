package Monitores;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Interfaces.IPollingStation;
import Threads.TVoter;

public class MPollingStation implements IPollingStation{

    private static MPollingStation instance;

    private final int capacity;
    private boolean isOpen;
    // private final Set<Integer> usedIds;
    private boolean isAproved;

    private final Integer[] votersInside;
    private int front;
    private int rear;
    private int count;
    private final ReentrantLock queue_lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private final Condition stationOpen;

   

    
    private MPollingStation(int capacity) {
        this.capacity = capacity;
        this.isOpen = false;
        // this.usedIds = new HashSet<>();

        this.isAproved = true;

        votersInside = new Integer[capacity];
        front = 0;
        rear = -1;
        count = 0;

        this.queue_lock = new ReentrantLock();
        this.notEmpty = queue_lock.newCondition();
        this.notFull = queue_lock.newCondition();
        this.stationOpen = queue_lock.newCondition();

        // this.messageBoard = new HashMap<>();
        // this.hash_lock = new ReentrantLock();
        // this.newMessage = queue_lock.newCondition();
    }
    
    public static MPollingStation getInstance(int maxCapacity) {
        if (instance == null) {
            instance = new MPollingStation(maxCapacity);
        }
        return instance;
    }
    

    @Override
    public boolean enterPollingStation(int voterId) {
        queue_lock.lock();
        try {
            while (!isOpen) {
                stationOpen.await();
            }
            
            while (count >= capacity) {
                notFull.await();
            }
            
            rear = (rear + 1) % votersInside.length;
            votersInside[rear] = voterId;
            count++;
            notEmpty.signal();
            return true;

        } catch (InterruptedException e) {
            return false;
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
    public int callNextVoter(){
        queue_lock.lock();
        try {
            if (count == 0) {
                notEmpty.await();
            }
            int id = votersInside[front];
            front = (front + 1) % votersInside.length;
            count--;
            return id;
        } catch (InterruptedException e) {
            return -1;
        } finally {
            queue_lock.unlock();
        }
    }


    
    // @Override
    // public void exitPollingStation(int voterId) {
    //     lock.lock();
    //     try {
    //         votersInside.remove(voterId);
    //         stationNotFull.signal();
    //     } finally {
    //         lock.unlock();
    //     }
    // }

    // @Override
    // boolean validateID(int voterId);

    
    @Override
    public boolean waitIdValidation(int voterId) {
        lock.lock();
        try {
            if (usedIds.contains(voterId)) {
                return false;
            }
            
            // Simulate ID validation time (5-10ms)
            Thread.sleep(5 + (int)(Math.random() * 6));
            usedIds.add(voterId);
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void openPollingStation() {
        lock.lock();
        try {
            isOpen = true;
            stationOpen.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void closePollingStation() {
        lock.lock();
        try {
            isOpen = false;
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
}
