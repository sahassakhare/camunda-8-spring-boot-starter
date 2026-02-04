#!/bin/bash
# Install podman-compose (already done, but ensures it's there)
pip3 install podman-compose

echo "⚠️  Your Homebrew is in a shallow state. Fixing it now (this may take a few minutes)..."
git -C /usr/local/Homebrew/Library/Taps/homebrew/homebrew-core fetch --unshallow
git -C /usr/local/Homebrew/Library/Taps/homebrew/homebrew-cask fetch --unshallow

echo "Updating Homebrew..."
brew update

echo "Installing Podman..."
brew install podman

echo "Initializing Podman Machine..."
podman machine init
podman machine start

echo "Starting Camunda 8..."
podman-compose up -d
