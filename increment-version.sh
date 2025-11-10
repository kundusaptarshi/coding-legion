#!/bin/bash
# Auto-increment patch version if there are NEW code changes since last build

VERSION_FILE="version.properties"
LAST_BUILD_HASH_FILE=".last-build-hash"
LAST_BUILD_VERSION_FILE=".last-build-version"

# Read current version
CURRENT_VERSION=$(grep "version=" $VERSION_FILE | cut -d'=' -f2)

# Check if version was manually changed (different from last build)
if [ -f "$LAST_BUILD_VERSION_FILE" ]; then
    LAST_VERSION=$(cat "$LAST_BUILD_VERSION_FILE")
    if [ "$CURRENT_VERSION" != "$LAST_VERSION" ]; then
        echo "ℹ️  Version manually changed ($LAST_VERSION → $CURRENT_VERSION) - skipping auto-increment"
        echo "$CURRENT_VERSION" > "$LAST_BUILD_VERSION_FILE"
        exit 0
    fi
fi

# Generate hash of all files in src/ directory (works without git)
# Uses file content to detect changes
CURRENT_HASH=$(find src/ -type f -name "*.java" -o -name "*.xml" -o -name "*.properties" 2>/dev/null | \
               sort | \
               xargs cat 2>/dev/null | \
               md5 2>/dev/null || \
               find src/ -type f -name "*.java" -o -name "*.xml" -o -name "*.properties" 2>/dev/null | \
               sort | \
               xargs cat 2>/dev/null | \
               md5sum 2>/dev/null | \
               cut -d' ' -f1)

# Check if this is the same code state as last build
if [ -f "$LAST_BUILD_HASH_FILE" ]; then
    LAST_HASH=$(cat "$LAST_BUILD_HASH_FILE")
    if [ "$CURRENT_HASH" = "$LAST_HASH" ]; then
        echo "ℹ️  No NEW code changes since last build - version unchanged"
        exit 0
    fi
fi

# There are new code changes - increment patch version

# Split version into major.minor.patch
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

# Increment patch version
NEW_PATCH=$((PATCH + 1))
NEW_VERSION="$MAJOR.$MINOR.$NEW_PATCH"

# Update version.properties
echo "version=$NEW_VERSION" > $VERSION_FILE

# Save current state for next build
echo "$CURRENT_HASH" > "$LAST_BUILD_HASH_FILE"
echo "$NEW_VERSION" > "$LAST_BUILD_VERSION_FILE"

echo "✅ Version incremented: $CURRENT_VERSION → $NEW_VERSION"
echo "📝 New changes detected in src/ directory"

