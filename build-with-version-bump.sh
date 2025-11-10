#!/bin/bash
# Build plugin with automatic version increment if code changed

echo "🔍 Checking for code changes..."

# Increment version if code changed
./increment-version.sh

# Build the plugin
echo ""
echo "🏗️  Building plugin..."
./gradlew clean buildPlugin

if [ $? -eq 0 ]; then
    CURRENT_VERSION=$(grep "version=" version.properties | cut -d'=' -f2)
    echo ""
    echo "✅ Build successful!"
    echo "📦 Plugin: build/distributions/coding-legion-$CURRENT_VERSION.zip"
    echo ""
    echo "💡 Tip: Commit the version change:"
    echo "   git add version.properties"
    echo "   git commit -m \"Bump version to $CURRENT_VERSION\""
else
    echo ""
    echo "❌ Build failed"
    exit 1
fi

