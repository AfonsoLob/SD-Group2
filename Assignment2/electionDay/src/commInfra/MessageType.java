package commInfra;

/**
 * Enum defining all message types for the Election Day simulation system.
 * This enum classifies the different types of messages exchanged between
 * clients and servers in the distributed polling station simulation.
 */

public enum MessageType {
    // Polling station status messages
    POLLING_STATION_OPEN,              // Sent BY Poll Clerk to notify voters that polling station is open
    POLLING_STATION_READY,             // Sent TO Poll Clerk to notify that polling station is ready
    POLLING_STATION_CLOSE,             // Sent BY Poll Clerk to notify that polling station is closed
    POLLING_STATION_CLOSED,            // Sent TO Poll Clerk to notify that polling station is closed
    POLLING_STATION_IS_OPEN,            // Sent TO Poll Clerk to notify that polling station is open
    VALIDATE_NEXT_VOTER,               // Sent by Poll Clerk to validate the next voter

    // POLLING_STATION_FULL,              // Sent when polling station reaches capacity
    // POLLING_STATION_HAS_SPACE,         // Sent when space becomes available in polling station
    
    // Voter action messages
    VOTER_ENTER_REQUEST,               // Voter requests to enter polling station
    VOTER_ENTER_GRANTED,               // Voter allowed to enter polling station
    VOTER_EXIT,                        // Voter exits polling station
    VOTER_REBORN,                      // Voter is "reborn" with new or same ID

    VOTERS_QUEUE_REQUEST,             // Request for the number of voters in queue
    VOTERS_QUEUE_RESPONSE,            // Response with the number of voters in queue
    
    // ID verification messages
    ID_CHECK_REQUEST,                  // Voter requests ID verification
    ID_VALID,                          // ID is validated successfully
    ID_INVALID,                        // ID is invalid or has already voted
    
    // Voting booth messages
    VOTE_CAST_REQUEST,                  // Voter requests access to e-voting booth
    VOTE_CAST_DONE,                     // Access to e-voting booth granted
    
    // Exit poll messages
    EXIT_POLL_ENTER,                // Voter enters exit poll
    EXIT_POLL_LEAVE,                // Exitpoll response after voter leaves

    EXIT_POLL_INQUIRY,              // Pollster inquiry
    EXIT_POLL_RESPONSE,             // Exit poll inquiry response

    EXIT_POLL_CLOSE,                // Clerk closes exit poll
    EXIT_POLL_OPEN,             // Pollster ask if exitpoll opened

    EXIT_POLL_OPENED,                // Exit poll opened
    EXIT_POLL_CLOSED,               // Exit poll closed

    EXIT_POLL_PRINT,                 // Pollster prints exit poll results
    EXIT_POLL_PRINTED,              // Exit poll results printed

    
    // Election result messages
    ELECTION_RESULTS_REQUEST,          // Request for current election results
    ELECTION_RESULTS_RESPONSE,         // Response with current election results
    
    // System control messages
    SIMULATION_END,                    // Signal to end the simulation
    SIMULATION_STATUS_REQUEST,         // Request for overall simulation status
    SIMULATION_STATUS_RESPONSE,        // Response with overall simulation status
    
    // Error messages
    ERROR,                              // General error message

    // Logger event messages (Client -> Logger)
    LOG_VOTER_AT_DOOR,              // int voterId
    LOG_VOTER_ENTERING_QUEUE,       // int voterId
    LOG_VALIDATING_VOTER,           // int voterId, boolean valid
    LOG_VOTER_IN_BOOTH,             // int voterId, boolean voteA
    LOG_EXIT_POLL_VOTE,             // int voterId, String vote (can be empty)
    LOG_STATION_OPENING,
    LOG_STATION_CLOSING,

    // Logger remaining messages 
    REQ_VOTE_COUNTS,
    REP_VOTE_COUNTS,
    REQ_VOTERS_PROCESSED,
    REP_VOTERS_PROCESSED,
    REQ_IS_STATION_OPEN,
    REP_IS_STATION_OPEN,
    REQ_CURRENT_VOTER_IN_BOOTH,
    REP_CURRENT_VOTER_IN_BOOTH,
    REQ_CURRENT_QUEUE_SIZE,
    REP_CURRENT_QUEUE_SIZE,

    LOGGER_TERMINATED,

    LOG_ACK,                          // Acknowledgment message for logger events
    // SIMULATION_END is already defined and can be used to trigger logger's saveCloseFile

}