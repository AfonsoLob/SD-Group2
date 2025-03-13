package Monitores;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Interfaces.IPollingStation;

public class MPollingStation implements IPollingStation{
    private final int capacity;
    private boolean isOpen;
    private final Set<Integer> usedIds;
    private final Queue<Integer> votersInside;
    private final ReentrantLock lock;
    private final Condition stationNotFull;
    private final Condition stationOpen;
    
    public MPollingStation(int capacity) {
        this.capacity = capacity;
        this.isOpen = false;
        this.usedIds = new HashSet<>();
        this.votersInside = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.stationNotFull = lock.newCondition();
        this.stationOpen = lock.newCondition();
    }
    
    public IPollingStation getInstance() {
        return this;
    }
    
    @Override
    public boolean enterPollingStation(int voterId) {
        lock.lock();
        try {
            while (!isOpen) {
                stationOpen.await();
            }
            
            while (votersInside.size() >= capacity) {
                stationNotFull.await();
            }
            
            votersInside.add(voterId);
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void exitPollingStation(int voterId) {
        lock.lock();
        try {
            votersInside.remove(voterId);
            stationNotFull.signal();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public boolean validateID(int voterId) {
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
