#!/bin/bash

#!/**
# This script is used to start the SafePath JDBC application.
# It checks the Java and Maven versions, and starts the application using Jetty.
# It also prints the Java and Maven versions, the port number, and the URL to access the application.
#
# Usage:
#   ./run.sh
#
# This script will start the application using Jetty.
# It will also print the Java and Maven versions, the port number, and the URL to access the application.
#
# The application will be available at http://localhost:9090
#*/

# set Java environment
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.7/libexec/openjdk.jdk/Contents/Home

# check Java
if [ ! -d "$JAVA_HOME" ]; then
    echo "❌ Java 21 not found, trying to find automatically..."
    JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
    if [ -z "$JAVA_HOME" ]; then
        echo "❌ Please install Java 21 first"
        exit 1
    fi
    echo "✅ Found Java: $JAVA_HOME"
fi

# check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found, please install Maven first"
    exit 1
fi

echo "=========================================="
echo "🚀 Start SafePath JDBC application"
echo "=========================================="
echo "Java Home: $JAVA_HOME"
echo "Maven: $(mvn -version | head -1)"
echo "Port: 9090"
echo ""
echo "After starting, access:"
echo "  - Home: http://localhost:9090"
echo "  - API:  http://localhost:9090/crime-report?action=list"
echo ""
echo "Press Ctrl+C to stop the server"
echo "=========================================="
echo ""

# start Jetty
mvn jetty:run

