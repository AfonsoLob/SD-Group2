#!/bin/bash
# RunAll.sh - Script to run the entire Election Day RMI application

# Define Project Root relative to the script directory
PROJECT_ROOT=".."
SCRIPTS_DIR="." # Current directory
BIN_DIR="$PROJECT_ROOT/bin"
CLASSPATH=".:$BIN_DIR" # Assuming script is run from project root, or adjust BIN_DIR

echo "Starting Election Day RMI Application..."
echo "Ensure you have compiled the project using Compile.sh first!"
echo "All components will start simultaneously. The Logger GUI will launch automatically."
echo ""

# Function to run a command in a new terminal (example for gnome-terminal)
run_in_new_terminal() {
    title="$1"
    command_to_run="$2"
    gnome-terminal --title="$title" -- bash -c "$command_to_run; exec bash"
    # For macOS: osascript -e "tell app \"Terminal\" to do script \"$command_to_run\""
    # For Windows (using start): start "$title" cmd /c "$command_to_run"
}

echo "Starting all servers simultaneously..."

# 0. Start RegisterRemoteObjectServer FIRST (all other servers depend on this)
echo "  - Starting RegisterRemoteObjectServer (registry service)..."
run_in_new_terminal "Register Service" "java -cp \"$CLASSPATH\" serverSide.main.RegisterRemoteObjectServer 22351 localhost 22350"

# 1. Start LoggerServer (Main Orchestrator with auto-launched GUI)
echo "  - Starting LoggerServer (with auto-launched GUI)..."
run_in_new_terminal "Logger Server" "java -cp \"$CLASSPATH\" serverSide.main.LoggerServer"

# 2. Start PollingStationServer
echo "  - Starting PollingStationServer..."
run_in_new_terminal "Polling Station Server" "java -cp \"$CLASSPATH\" serverSide.main.PollingStationServer"

# 3. Start ExitPollServer
echo "  - Starting ExitPollServer..."
run_in_new_terminal "Exit Poll Server" "java -cp \"$CLASSPATH\" serverSide.main.ExitPollServer"

# Give servers a moment to initialize and register
echo "Allowing RegisterService and servers to initialize (5 seconds)..."
sleep 5

echo "Starting all client applications..."

# 4. Start ClerkClient
echo "  - Starting ClerkClient..."
run_in_new_terminal "Clerk Client" "java -cp \"$CLASSPATH\" clientSide.main.ClerkClient"

# 5. Start PollsterClient
echo "  - Starting PollsterClient..."
run_in_new_terminal "Pollster Client" "java -cp \"$CLASSPATH\" clientSide.main.PollsterClient"

# 6. Start VoterClient
echo "  - Starting VoterClient..."
run_in_new_terminal "Voter Client" "java -cp \"$CLASSPATH\" clientSide.main.VoterClient"

echo ""
echo "=========================================="
echo "Election Day Application Started!"
echo "=========================================="
echo "Components launched:"
echo "  - Register Service (RMI registry manager)"
echo "  - Logger Server (with GUI) - Configure parameters via GUI"
echo "  - Polling Station Server"
echo "  - Exit Poll Server"
echo "  - Clerk Client"
echo "  - Pollster Client"
echo "  - Voter Client"
echo ""
echo "NEXT STEPS:"
echo "1. Use the Logger GUI to set simulation parameters"
echo "2. The simulation will start automatically once parameters are configured"
echo "3. Monitor individual terminal windows for component output"
echo ""
echo "To stop the simulation: Close/kill each terminal window or use Ctrl+C"
echo "=========================================="