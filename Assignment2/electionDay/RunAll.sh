echo "Running all scripts"

# Build everything first
./BuildAll.sh

if [ $? -ne 0 ]; then
    echo "Build failed, cannot run components."
    exit 1
fi

# Start servers in background
echo -e "\nStarting Logger Server..."
./RunServerLogger.sh &
# Wait for Logger to initialize
sleep 3

echo -e "\nStarting Polling Station Server..."
./RunServerPollingStation.sh &
# Wait for Polling Station to initialize
sleep 3

echo -e "\nStarting Exit Poll Server..."
./RunServerExitPoll.sh &
# Wait for Exit Poll to initialize
sleep 3

# Start clients
echo -e "\nStarting Clerk Client..."
./RunClientClerk.sh &
sleep 1

echo -e "\nStarting Pollster Client..."
./RunClientPollster.sh &
sleep 1

echo -e "\nStarting Voter Clients..."
./RunClientVoter.sh

echo -e "\nAll components are now running on localhost with different ports:"
echo "- Logger Server: localhost:4002"
echo "- Polling Station Server: localhost:4000"
echo "- Exit Poll Server: localhost:4001"