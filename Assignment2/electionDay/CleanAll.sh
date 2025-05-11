#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment2/electionDay/CleanAll.sh

echo "Starting full cleanup of compiled class files..."

# Delete any log files that might have been created
echo "Removing log files..."
find . -name "*.log" -type f -delete
find . -name "log.txt" -type f -delete

# Clean all class files in the src directory (recursive)
echo "Removing .class files from src directory..."
find ./src -name "*.class" -type f -delete

# Clean all class files in the bin directory (recursive)
if [ -d "./bin" ]; then
    echo "Cleaning bin directory..."
    find ./bin -name "*.class" -type f -delete
    # Optionally remove the bin directory entirely
    rm -rf ./bin
    echo "Removed bin directory and all its contents"
    
    # Recreate empty bin directory
    mkdir -p bin
    echo "Created fresh bin directory"
else
    echo "bin directory not found (already clean)"
    mkdir -p bin
    echo "Created fresh bin directory"
fi

# Remove project_files.txt if it exists
if [ -f "./project_files.txt" ]; then
    rm -f ./project_files.txt
    echo "Removed project_files.txt"
fi

# Check for any other class files in the project
echo "Checking for any remaining class files..."
find . -name "*.class" -type f -delete

# Count remaining class files to verify cleanup
remaining=$(find . -name "*.class" | wc -l)
if [ $remaining -eq 0 ]; then
    echo "Cleanup complete. All class files have been removed."
else
    echo "Warning: $remaining class files could not be removed. Check file permissions."
    echo "Locations of remaining class files:"
    find . -name "*.class" -type f
fi

echo "Cleanup finished! Your project is clean and ready for a fresh build."