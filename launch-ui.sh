#!/bin/bash
# Launch Hospital Management System UI

echo "======================================"
echo " Hospital Management System"
echo " Swing UI Application"
echo "======================================"
echo ""

# Check if JAR exists
if [ ! -f "target/hospital-1.0-SNAPSHOT.jar" ]; then
    echo "Building application..."
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "Build failed! Please check errors above."
        exit 1
    fi
fi

echo "Launching UI..."
echo ""
java -cp target/hospital-1.0-SNAPSHOT.jar org.hospital.AppUI

# Alternative: Use maven exec
# mvn exec:java -Dexec.mainClass="org.hospital.AppUI" -q

