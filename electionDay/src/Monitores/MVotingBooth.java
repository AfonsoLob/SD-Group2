package Monitores;

import Interfaces.IVotingBooth;
import java.util.concurrent.locks.ReentrantLock;
import Logging.Logger;

public class MVotingBooth implements IVotingBooth {
    private static MVotingBooth instance;
    private final ReentrantLock lock;
    private final Logger logger;

    private int candidateA;
    private int candidateB;

    private MVotingBooth(Logger logger) {
        this.candidateA = 0;
        this.candidateB = 0;
        this.lock = new ReentrantLock();
        this.logger = logger;
    }

    public static MVotingBooth getInstance(Logger logger) {
        if (instance == null) {
            instance = new MVotingBooth(logger);
        }
        return instance;
    }

    @Override
    public void voteA(int voterId) {
        lock.lock();
        try {
            Thread.sleep((long) (Math.random() * 15)); // 0-15 ms
            candidateA++;
            System.out.println("A total votes: " + candidateA);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            logger.voterInBooth(voterId, true); // vote A
            lock.unlock();
        }
        
    }

    @Override
    public void voteB(int voterId) {
        lock.lock();
        try {
            Thread.sleep((long) (Math.random() * 15)); // 0-15 ms
            candidateB++;
            System.out.println("B total votes: " + candidateA);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            logger.voterInBooth(voterId, false); // vote B
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
