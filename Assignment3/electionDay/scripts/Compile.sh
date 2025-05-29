#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment3/electionDay/scripts/Compile.sh

# Get the current directory
# DIR=$(pwd) # Not strictly necessary for this script if paths are relative to script location or project root

# Define source and output directories
# Assuming this script is in the 'scripts' directory and 'src' and 'bin' are at the project root (one level up)
PROJECT_ROOT=".."
SRC_DIR="$PROJECT_ROOT/src"
BIN_DIR="$PROJECT_ROOT/bin"

# Remove old bin directory and create a new one
echo "Removing old bin directory..."
rm -rf "$BIN_DIR"
echo "Creating new bin directory..."
mkdir -p "$BIN_DIR"

# Set the classpath to include any necessary libraries (if any)
# For this project, it seems we don't have external .jar libs other than standard Java RMI/Swing
# CLASSPATH="."
# if [ -d "$PROJECT_ROOT/lib" ]; then
#   for jar in $PROJECT_ROOT/lib/*.jar; do
#     CLASSPATH="$CLASSPATH:$jar"
#   done
# fi

# Compile all .java files from the src directory to the bin directory
# Using -Xlint for more detailed warnings, which is good practice
echo "Compiling Java source files..."
find "$SRC_DIR" -name "*.java" > sources.txt
javac -Xlint:all -d "$BIN_DIR" @sources.txt
# javac -Xlint:all -cp "$CLASSPATH" -d "$BIN_DIR" $(find "$SRC_DIR" -name "*.java")

# Check if compilation was successful
if [ $? -eq 0 ]; then
  echo "Compilation successful."
else
  echo "Compilation failed. Please check the errors above."
  rm sources.txt
  exit 1
fi

rm sources.txt
echo "Java .class files are in $BIN_DIR"

# Reminder to make scripts executable (run once manually)
# echo "If you haven't already, make your run scripts executable, e.g.:"
# echo "chmod +x *.sh"
