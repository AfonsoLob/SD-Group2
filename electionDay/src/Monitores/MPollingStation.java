package Monitores;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Interfaces.IPollingStation;
import Threads.*;

public class MPollingStation implements IPollingStation{

    private static MPollingStation instance;

    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private final TVoter[] queue;
    private int front;
    private int rear;
    private int count;


    
    private MPollingStation(int maxCapacity) {
        queue = new TVoter[maxCapacity];
        front = 0;
        rear = -1;
        count = 0;

    }

    public static synchronized MPollingStation getInstance(int maxCapacity) {
        if (instance == null) {
            instance = new MPollingStation(maxCapacity);
        }
        return instance;
    }

    public void enterStation(TVoter voter) {
        lock.lock();
        try {
            if (count == queue.length) {
                // alguma coisa 
            }
            rear = (rear + 1) % queue.length;
            queue[rear] = voter;
            count++;
            // System.out.println(c.getCustomerName() + " is waiting inside the barber shop");
            notEmpty.signal(); // notify sleeping ...
        } finally {
            lock.unlock();
        }
    }

    }

    public synchronized void exitStation(TVoter voter) {
        currentVoters--;
        notifyAll();
    }
}
