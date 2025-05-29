#!/bin/bash
# filepath: /home/weza/Documents/UA/Year-1/S-2/MECT-A_1-S_2/SD/SD-Group2/Assignment2/electionDay/MakeRunnable.sh

echo "Making all shell scripts executable..."

# Find all shell scripts in the current directory and make them executable
find . -maxdepth 1 -name "*.sh" -exec chmod +x {} \;

echo "Scripts made executable:"
find . -maxdepth 1 -name "*.sh" -exec ls -la {} \;

echo "Done!"