#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment2/electionDay/BuildAll.sh

echo "Building all project components..."

# Current directory - should be the project root (electionDay/)
CURRENT_DIR=$(pwd)
echo "Current directory: $CURRENT_DIR"

# Create bin directory if it doesn't exist
mkdir -p bin

# Clean previous build
rm -rf bin/*
echo "Cleaned bin directory"

# Check if required source files exist
echo "Checking for required source files..."
find src -name "*.java" | sort > project_files.txt
echo "Found $(wc -l < project_files.txt) Java files (see project_files.txt for details)"

# Create a classpath that includes the required libraries
CLASSPATH=".:bin"

# Add any third-party JAR files if they exist
if [ -d "lib" ]; then
  for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi

echo "Using classpath: $CLASSPATH"

# Compile all Java files together
echo "Compiling all Java files..."
javac -cp "$CLASSPATH" -d bin $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed! Please check the error messages above."
    exit 1
fi

# Make the script executable
chmod +x BuildAll.sh

echo "Build completed successfully."
echo "To run servers and clients, use the RunAll.sh script or individual run scripts."