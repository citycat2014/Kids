#!/bin/bash

echo "Starting compilation test..."

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java to test compilation."
    exit 1
fi

# Try to compile just the Kotlin files to check for syntax errors
echo "Checking Kotlin syntax..."

# Find all Kotlin files and compile them
find app/src/main/java -name "*.kt" -exec echo "Checking: {}" \; -exec kotlinc -cp "$(find ~/.gradle/caches -name "kotlin-stdlib-*.jar" | head -1 | tr '\n' ':')" -no-jdk {} \; 2>&1 | grep -E "(error:|warning:)" || echo "Kotlin syntax check completed"

echo "Compilation test complete."