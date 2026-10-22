#!/bin/bash
# 1-Click Update Script for Replit and Linux environments
# Forces local workspace to match GitHub main 100% without divergent branch conflicts.

echo ">>> Fetching latest updates from GitHub..."
git fetch origin main

echo ">>> Resetting local branch to match origin/main..."
git reset --hard origin/main

echo ">>> Repository successfully updated to latest commit:"
git log -1 --oneline
