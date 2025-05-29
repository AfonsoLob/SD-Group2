#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment3/electionDay/scripts/RunClientVoter.sh

# Get the current directory
DIR=$(pwd)

# Set the classpath to include the bin directory and any jar files
CLASSPATH=".:bin"
if [ -d "lib" ]; then
  for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi

# Run the Voter client
echo "Starting Voter Client connecting to localhost servers..."
java -cp "$CLASSPATH" clientSide.main.VoterClient

# Make the script executable
# chmod +x RunClientVoter.sh # This line is usually run once manually, not every time the script runs.
