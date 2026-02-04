#!/bin/bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
echo "Using JAVA_HOME: $JAVA_HOME"
"$JAVA_HOME/bin/java" -version

echo "Building project..."
mvn clean install

if [ $? -eq 0 ]; then
  echo "Build successful. Starting simple-app..."
  cd examples/simple-app
  mvn spring-boot:run
else
  echo "Build failed. Skipping application run."
  exit 1
fi
