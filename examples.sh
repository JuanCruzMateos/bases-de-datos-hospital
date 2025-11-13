#!/bin/bash
# Hospital Database - CRUD Examples
# This script demonstrates how to use the CLI commands

echo "=== Hospital Database CRUD Examples ==="
echo ""

echo "1. Listing all medicos..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="list-medicos" -q
echo ""

echo "2. Getting a specific medico (matricula 1001)..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="get-medico 1001" -q
echo ""

echo "3. Listing all pacientes..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="list-pacientes" -q
echo ""

echo "4. Getting a specific paciente (DNI 32456789)..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="get-paciente DNI 32456789" -q
echo ""

echo "5. Listing all sectores..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="list-sectores" -q
echo ""

echo "6. Listing all habitaciones..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="list-habitaciones" -q
echo ""

echo "7. Listing active internaciones..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="list-internaciones-activas" -q
echo ""

echo "8. Listing all guardias..."
mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="list-guardias" -q
echo ""

# Uncomment to test create operations:
# echo "9. Creating a new paciente..."
# mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="create-paciente DNI 99999999 Test Patient PACIENTE 1995-05-20 M" -q
# echo ""

# echo "10. Creating a new sector..."
# mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="create-sector 'Emergencias'" -q
# echo ""

# echo "11. Creating a new habitacion..."
# mvn exec:java -Dexec.mainClass="org.hospital.App" -Dexec.args="create-habitacion 3 NORTE 1" -q
# echo ""

echo "Done! All examples completed successfully."

