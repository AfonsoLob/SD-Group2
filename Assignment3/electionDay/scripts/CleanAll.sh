#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment3/electionDay/scripts/CleanAll.sh

echo "Starting full cleanup of compiled class files and logs..."

# Define Project Root relative to the script directory (scripts/ -> ../)
PROJECT_ROOT=".."

# Delete any log files that might have been created in the project root or subdirectories
echo "Removing log files from project root ($PROJECT_ROOT)..."
find "$PROJECT_ROOT" -name "*.log" -type f -delete
find "$PROJECT_ROOT" -name "log.txt" -type f -delete # Specifically target log.txt if it can be anywhere

# Clean all class files in the src directory (recursive) - defensive coding, should not be there
echo "Removing any .class files from src directory ($PROJECT_ROOT/src)..."
find "$PROJECT_ROOT/src" -name "*.class" -type f -delete

# Clean all class files in the bin directory (recursive)
if [ -d "$PROJECT_ROOT/bin" ]; then
    echo "Cleaning bin directory ($PROJECT_ROOT/bin)..."
    # find "$PROJECT_ROOT/bin" -name "*.class" -type f -delete # Not needed if removing the whole directory
    # Optionally remove the bin directory entirely
    rm -rf "$PROJECT_ROOT/bin"
    echo "Removed bin directory ($PROJECT_ROOT/bin) and all its contents"
    
    # Recreate empty bin directory
    mkdir -p "$PROJECT_ROOT/bin"
    echo "Created fresh bin directory ($PROJECT_ROOT/bin)"
else
    echo "bin directory ($PROJECT_ROOT/bin) not found (already clean or never created)"
    mkdir -p "$PROJECT_ROOT/bin"
    echo "Created fresh bin directory ($PROJECT_ROOT/bin)"
fi

# Remove project_files.txt if it exists (assuming it might be in project root or scripts dir)
if [ -f "$PROJECT_ROOT/project_files.txt" ]; then
    rm -f "$PROJECT_ROOT/project_files.txt"
    echo "Removed $PROJECT_ROOT/project_files.txt"
elif [ -f "./project_files.txt" ]; then # Check in current (scripts) dir too
    rm -f "./project_files.txt"
    echo "Removed ./project_files.txt"
fi

# Check for any other class files in the project root
echo "Checking for any remaining class files in project root ($PROJECT_ROOT)..."
# This find should ideally target specific areas if class files could be elsewhere, 
# but a broad search from project root is a strong cleanup measure.
# Be cautious if there are other Java projects or compiled files in unrelated subdirectories of PROJECT_ROOT.
find "$PROJECT_ROOT" -path "$PROJECT_ROOT/src" -prune -o -path "$PROJECT_ROOT/scripts" -prune -o -name "*.class" -type f -delete
# The above tries to exclude src and scripts from the final sweep if they are under PROJECT_ROOT, focusing on other areas.
# A simpler, more aggressive sweep from project root (excluding bin as it's recreated):
# find "$PROJECT_ROOT" -not -path "$PROJECT_ROOT/bin/*" -name "*.class" -type f -delete


# Count remaining class files to verify cleanup (excluding bin as it's fresh and empty)
echo "Verifying cleanup (checking for .class files outside the fresh bin dir)..."
remaining=$(find "$PROJECT_ROOT" -not -path "$PROJECT_ROOT/bin/*" -name "*.class" | wc -l)
if [ $remaining -eq 0 ]; then
    echo "Cleanup complete. All class files (outside the fresh bin directory) have been removed."
else
    echo "Warning: $remaining class files could not be removed or were found outside expected locations (and outside the fresh bin). Check file permissions or project structure."
    echo "Locations of remaining class files (outside bin):"
    find "$PROJECT_ROOT" -not -path "$PROJECT_ROOT/bin/*" -name "*.class" -type f
fi

echo "Cleanup finished! Your project is clean and ready for a fresh build."