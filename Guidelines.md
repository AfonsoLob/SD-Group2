# Election Day Simulation - Implementation Breakdown

## 1. Characterize State-Level Interaction
### Entities & Shared State
- **Voters**: Enter, validate ID, vote, exit, and go through reborn state.
- **E-Voting Booth**: Handles the actual voting process.
- **Poll Clerk**: Manages polling station, ID verification, and voting process.
- **Pollster**: Approaches voters for exit polls.
- **Polling Station**: Controls entry, waiting queue, and synchronization.

### State Variables & Transformations
#### 1. Polling Station
- `queueSize`: Tracks the number of voters inside the polling station.
#### 2. Pollster
- `pollResponses`: Collects pollster responses.
#### 3. Poll Clerk
- `validIDs`: Stores IDs to prevent duplicate voting.
#### 4. E-Voting Booth
- `boothAvailable`: Boolean flag to indicate if the e-voting booth is available.
- `votes`: Stores votes cast for each candidate.
---

## 2. Specify the Life Cycle and Internal Properties of Each Entity
### **Voter Life Cycle**
1. Created, enters polling station, waits in queue.
2. ID validation → If valid, proceeds to voting booth
3. Votes and exits the booth.
4. Chance of responding to exit poll.
5. Leaves and may be reborn.

### **Poll Clerk Life Cycle**
1. Starts when station opens.
2. Validates voter IDs.
3. Sends voters to the **e-voting booth** one by one.
4. Closes station after a predefined number of voters.

### **E-Voting Booth Life Cycle**
1. Waits for a voter.
2. Processes the vote.
3. Notifies poll clerk once voting is done.
4. Becomes available for the next voter.

### **Pollster Life Cycle**
1. Intercepts voters randomly (based on a probability).
2. Requests responses.

---

## 3. Define Information Sharing Regions
### **Data Structures & Operations**
- `Queue<Voter>` for waiting area (review: probably ConcurrentQueue as well):
  - `enqueue()`, `dequeue()`
- `HashSet<Integer>` for used voter IDs:
  - `add()`, `contains()`
- 2x `HashMap<String, Integer>` for votes (voting booth & pollster):
  - `updateVote()`
- `ConcurrentQueue<Voter>` for exit polling:
  - `addResponse()`

### **Synchronization Points**
- **Entry Queue**: Monitor/Semaphore to limit voters.
- **ID Validation & Voting**: Ensure only one voter at a time.
- **E-Voting Booth Access**: Mutex/Monitor to prevent simultaneous usage.
- **Exit Poll**: Mutex for accessing voter responses.?
