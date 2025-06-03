#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment3/electionDay/scripts/RunServerLogger.sh

# Get the current directory
DIR=$(pwd)

# Define Project Root relative to the script directory (scripts/ -> ../)
PROJECT_ROOT=".."

# Set the classpath to include the bin directory and any jar files
CLASSPATH=".:$PROJECT_ROOT/bin"
if [ -d "$PROJECT_ROOT/lib" ]; then
  for jar in $PROJECT_ROOT/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi

# Run the Logger server
echo "Starting Logger Server (Main Orchestrator with GUI)..."
java -cp "$CLASSPATH" serverSide.main.LoggerServer

# Make the script executable
# chmod +x RunServerLogger.sh # This line is usually run once manually, not every time the script runs.
