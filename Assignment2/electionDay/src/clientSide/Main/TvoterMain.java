package clientSide.Main;

import clientSide.entities.TVoter;
import clientSide.stubs.MPollingStationStub;
import clientSide.stubs.MExitPollStub;

import serverSide.main.SimulPar;

public class TvoterMain {
    public static void main(String[] args) {
        SimulPar.validateParameters();
        
        // Stubs
        MPollingStationStub pollingStation = new MPollingStationStub();
        MExitPollStub exitPoll = new MExitPollStub();
        
        // Create and start voter threads
        Thread[] voters = new Thread[SimulPar.N];
        for (int i = 0; i < SimulPar.N; i++) {
            voters[i] = new Thread(TVoter.getInstance(i, pollingStation, exitPoll));
            voters[i].start();
        }

        // Wait for all voters to finish
        for (int i = 0; i < SimulPar.N; i++) {
            try {
                voters[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
