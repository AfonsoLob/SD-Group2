package Interfaces;
import Threads.*;

public interface IPollingStation {
    void enterStation(TVoter voter); // tries to enter the polling station and waits in line 
    void validateID(TVoter voter);
    void vote(TVoter voter);
    void exitStation(TVoter voter);
}
