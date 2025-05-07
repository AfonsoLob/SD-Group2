echo "Running all scripts"

./RunClientVoter.sh
./RunClientClerk.sh
./RunClientPollster.sh

./RunServerPollingStation.sh
./RunServerExitPoll.sh
./RunServerGUI.sh

echo "All scripts succecfully executed"