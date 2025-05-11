
#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment2/electionDay/RunServerExitPoll.sh

# Get the current directory
DIR=$(pwd)

# Set the classpath to include the bin directory and any jar files
CLASSPATH=".:bin"
if [ -d "lib" ]; then
  for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi

# Run the Exit Poll server
echo "Starting Exit Poll Server on localhost:4001..."
java -cp "$CLASSPATH" serverSide.main.ServerExitPoll

# Make the script executable
chmod +x RunServerExitPoll.sh
