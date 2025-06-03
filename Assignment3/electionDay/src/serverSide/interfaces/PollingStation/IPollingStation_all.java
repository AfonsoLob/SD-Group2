package serverSide.interfaces.PollingStation;

// This interface aggregates other PollingStation interfaces and extends Remote implicitly through them.
public interface IPollingStation_all extends IPollingStation_Voter, IPollingStation_Clerk, IPollingStation_ExitPoll {
    // No additional method signatures are typically needed here as they are inherited.
}
