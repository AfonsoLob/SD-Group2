
echo "Compiling the code"
javac -sourcepath src -d bin src/serverSide/main/ServerLogger.java
echo "Running the code"
java ./src/serverSide/main/ServerLogger.java
