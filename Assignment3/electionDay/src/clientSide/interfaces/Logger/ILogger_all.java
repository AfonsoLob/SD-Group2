package clientSide.interfaces.Logger;


public interface ILogger_all extends ILogger_PollingStation, ILogger_ExitPoll, ILogger_GUI, ILogger_Common{
    // Combined interface for comprehensive logger functionality
    // All methods inherited from parent interfaces must also throw RemoteException
    // and parent interfaces must also extend Remote.

}
