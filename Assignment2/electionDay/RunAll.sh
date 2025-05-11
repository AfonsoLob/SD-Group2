echo "Running all scripts"


echo "\nPolling Station Server"
./RunServerPollingStation.sh
echo "\nExit Poll Server"
./RunServerExitPoll.sh
echo "\nLogger Server"
./RunServerLogger.sh

echo "\nVoter Client"
./RunClientVoter.sh
echo "\nClerk Client"
./RunClientClerk.sh
echo "\nPollster Client"
./RunClientPollster.sh


echo "All scripts succecfully executed"