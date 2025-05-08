package clientSide.interfaces.Pollingstation;

public interface IPollingStation_Voter {
    boolean enterPollingStation(int voterId);
    boolean waitIdValidation(int voterId);
    void voteA(int voterId);
    void voteB(int voterId);
    boolean isOpen();
}
