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
            candidateA++;
            System.out.println("Voted for candidate A, total votes: " + candidateA);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void voteB() {
        lock.lock();
        try {
            candidateB++;
            System.out.println("Voted for candidate B, total votes: " + candidateB);
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
