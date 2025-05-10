echo "Running all scripts"

./RunClientVoter.sh
./RunClientClerk.sh
./RunClientPollster.sh

./RunServerPollingStation.sh
./RunServerExitPoll.sh
./RunServerLogger.sh

echo "All scripts succecfully executed"