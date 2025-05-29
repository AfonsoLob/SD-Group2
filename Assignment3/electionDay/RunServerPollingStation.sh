
#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment2/electionDay/RunServerPollingStation.sh

# Get the current directory
DIR=$(pwd)

# Set the classpath to include the bin directory and any jar files
CLASSPATH=".:bin"
if [ -d "lib" ]; then
  for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi

# Run the Polling Station server
echo "Starting Polling Station Server on localhost:4000..."
java -cp "$CLASSPATH" serverSide.main.ServerPollingStation

# Make the script executable
chmod +x RunServerPollingStation.sh
