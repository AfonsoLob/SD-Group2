// package commInfra;

/**
 * Enum defining all message types for the Election Day simulation system.
 * This enum classifies the different types of messages exchanged between
 * clients and servers in the distributed polling station simulation.
 */

public enum MessageType {
    // Polling station status messages
    POLLING_STATION_OPEN,              // Sent by Poll Clerk to notify voters that polling station is open
    POLLING_STATION_CLOSED,            // Sent by Poll Clerk to notify that polling station is closed
    POLLING_STATION_FULL,              // Sent when polling station reaches capacity
    POLLING_STATION_HAS_SPACE,         // Sent when space becomes available in polling station
    
    // Voter action messages
    VOTER_ENTER_REQUEST,               // Voter requests to enter polling station
    VOTER_ENTER_GRANTED,               // Voter allowed to enter polling station
    VOTER_EXIT,                        // Voter exits polling station
    VOTER_REBORN,                      // Voter is "reborn" with new or same ID
    
    // ID verification messages
    ID_CHECK_REQUEST,                  // Voter requests ID verification
    ID_VALID,                          // ID is validated successfully
    ID_INVALID,                        // ID is invalid or has already voted
    
    // Voting booth messages
    VOTING_BOOTH_REQUEST,              // Voter requests access to e-voting booth
    VOTING_BOOTH_GRANTED,              // Access to e-voting booth granted
    VOTE_CAST,                         // Voter casts their vote
    
    // Exit poll messages
    EXIT_POLL_REQUEST,                 // Pollster requests voter for exit poll
    EXIT_POLL_RESPONSE,                // Voter responds to exit poll
    EXIT_POLL_DECLINED,                // Voter declines to participate in exit poll
    
    // Election result messages
    ELECTION_RESULTS_REQUEST,          // Request for current election results
    ELECTION_RESULTS_RESPONSE,         // Response with current election results
    
    // System control messages
    SIMULATION_END,                    // Signal to end the simulation
    SIMULATION_STATUS_REQUEST,         // Request for overall simulation status
    SIMULATION_STATUS_RESPONSE,        // Response with overall simulation status
    
    // Error messages
    ERROR                              // General error message
}