package clientSide.interfaces.Pollingstation;

public interface IPollingStation_Clerk {
    boolean callNextVoter();
    void openPollingStation();
    void closePollingStation();
    int numberVotersInQueue();
    void printFinalResults();
}
