#!/bin/bash
# RunAll.sh - Script to run the entire Election Day RMI application

# Define Project Root relative to the script directory
PROJECT_ROOT=".."
SCRIPTS_DIR="." # Current directory
BIN_DIR="$PROJECT_ROOT/bin"
CLASSPATH=".:$BIN_DIR" # Assuming script is run from project root, or adjust BIN_DIR

echo "Ensure you have compiled the project using Compile.sh first!"
echo "This script will attempt to start all components."
echo "You will need to interact with the Logger GUI to set parameters."

# Function to run a command in a new terminal (example for gnome-terminal)
run_in_new_terminal() {
    title="$1"
    command_to_run="$2"
    gnome-terminal --title="$title" -- bash -c "$command_to_run; exec bash"
    # For macOS: osascript -e "tell app \"Terminal\" to do script \"$command_to_run\""
    # For Windows (using start): start "$title" cmd /c "$command_to_run"
}

# 1. Start LoggerServer (Main Orchestrator with GUI)
echo "Starting LoggerServer (GUI)..."
# Run LoggerServer in its own terminal because it's long-running and has a GUI
run_in_new_terminal "Logger Server" "java -cp \"$CLASSPATH\" serverSide.main.LoggerServer"
echo "IMPORTANT: Interact with the Logger GUI to set simulation parameters."
echo "Press Enter to continue AFTER parameters are set in the GUI..."
read

# 2. Start PollingStationServer
echo "Starting PollingStationServer..."
run_in_new_terminal "Polling Station Server" "java -cp \"$CLASSPATH\" serverSide.main.PollingStationServer"
sleep 2 # Give it a moment to start and register

# 3. Start ExitPollServer
echo "Starting ExitPollServer..."
run_in_new_terminal "Exit Poll Server" "java -cp \"$CLASSPATH\" serverSide.main.ExitPollServer"
sleep 2 # Give it a moment to start and register

echo "Servers should be starting up..."
echo "Press Enter to start client applications..."
read

# 4. Start ClerkClient
echo "Starting ClerkClient..."
run_in_new_terminal "Clerk Client" "java -cp \"$CLASSPATH\" clientSide.main.ClerkClient"
sleep 1

# 5. Start PollsterClient
echo "Starting PollsterClient..."
run_in_new_terminal "Pollster Client" "java -cp \"$CLASSPATH\" clientSide.main.PollsterClient"
sleep 1

# 6. Start VoterClient
echo "Starting VoterClient..."
run_in_new_terminal "Voter Client" "java -cp \"$CLASSPATH\" clientSide.main.VoterClient"

echo ""
echo "All components initiated. Check individual terminal windows for output."
echo "To stop the simulation, you will need to close/kill each server and client process."