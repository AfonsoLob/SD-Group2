package Monitores;

import Interfaces.IVotingBooth;
import java.util.concurrent.locks.ReentrantLock;

public class MVotingBooth implements IVotingBooth {
    private static MVotingBooth instance;
    private final ReentrantLock lock;

    private int candidateA;
    private int candidateB;

    private MVotingBooth() {
        this.candidateA = 0;
        this.candidateB = 0;
        this.lock = new ReentrantLock();
    }

    public static MVotingBooth getInstance() {
        if (instance == null) {
            instance = new MVotingBooth();
        }
        return instance;
    }

    @Override
    public void voteA() {
        lock.lock();
        try {
            Thread.sleep((long) (Math.random() * 15)); // 0-15 ms
            candidateA++;
            System.out.println("A total votes: " + candidateA);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void voteB() {
        lock.lock();
        try {
            Thread.sleep((long) (Math.random() * 15)); // 0-15 ms
            candidateB++;
            System.out.println("B total votes: " + candidateB);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void printFinalResults() {
        lock.lock();
        try {
            System.out.println("Final results: ");
            System.out.println("Candidate A: " + candidateA);
            System.out.println("Candidate B: " + candidateB);
        } finally {
            lock.unlock();
        }
    }
}
