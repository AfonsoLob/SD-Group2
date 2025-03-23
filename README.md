# SD-Assignment1: Election Day Simulation

A Java-based concurrent programming simulation of an election day, featuring voters, a clerk, and an exit pollster.

## Project Structure

```
electionDay/
├── src/
│   ├── App.java           # Main application entry point
│   ├── GUI/               # GUI components
│   ├── Interfaces/        # Interface definitions
│   ├── Logging/          # Logging functionality
│   ├── Monitores/        # Monitor classes for synchronization
│   └── Threads/          # Thread implementations
└── README.md
```

## Building the Project

From the project root directory:

```bash
# Create bin directory if it doesn't exist
mkdir -p electionDay/bin

# Compile the project
javac -d electionDay/bin electionDay/src/**/*.java
```

## Running the Simulation

From the project root directory:

```bash
cd electionDay/src
java App [options]
```

### Command Line Options

1. `<number>` : Set maximum number of active voters (default: 5)
2. `<number>` : Set polling station queue capacity (default: 2)
3. `<number>` : Set required number of votes to close the station (default: 10)

Example:
```bash
java App 10 3 15
```
## Controls
- Start button to begin the simulation
- Use the slider to adjust simulation speed
- Close button to end the simulation
- Real-time statistics display