#!/bin/bash
set -e

echo "🔧 Removing stale git lock file..."
rm -f /usr/local/Homebrew/Library/Taps/homebrew/homebrew-core/.git/shallow.lock

echo "🔄 Unshallowing homebrew-core (this is required for brew update to work)..."
# Check if it's a shallow clone before trying to unshallow
if [ -f /usr/local/Homebrew/Library/Taps/homebrew/homebrew-core/.git/shallow ]; then
    git -C /usr/local/Homebrew/Library/Taps/homebrew/homebrew-core fetch --unshallow
else
    echo "homebrew-core is already unshallowed."
fi

echo "🔄 Unshallowing homebrew-cask..."
if [ -f /usr/local/Homebrew/Library/Taps/homebrew/homebrew-cask/.git/shallow ]; then
    git -C /usr/local/Homebrew/Library/Taps/homebrew/homebrew-cask fetch --unshallow
else
    echo "homebrew-cask is already unshallowed."
fi

echo "⬆️  Updating Homebrew..."
brew update

echo "📦 Installing Podman..."
# Try standard install first, fallback to build-from-source if bottle is missing
if ! brew install podman; then
    echo "Standard install failed. Trying build from source..."
    brew install --build-from-source podman
fi

echo "⚙️  Initializing Podman Machine..."
# Check if podman is available now
if ! command -v podman &> /dev/null; then
    echo "❌ Podman installation failed. Exiting."
    exit 1
fi

podman machine init || echo "Machine may already exist, attempting to start..."
podman machine start

echo "🚀 Starting Camunda 8..."
podman-compose up -d

echo "✅ Done! You can now run './build.sh' to build and run the example app."
