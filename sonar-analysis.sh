#!/bin/bash

# SonarQube Analysis Script for Shop Manager
# This script provides options for running code quality analysis

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Shop Manager Code Quality Analysis ===${NC}"
echo ""
echo "Select an option:"
echo "1) Start SonarQube Server (Docker)"
echo "2) Run SonarQube Analysis (requires server running)"
echo "3) Run JaCoCo Coverage Only (no server needed)"
echo "4) Stop SonarQube Server"
echo "5) View Coverage Reports"
echo "6) Exit"
echo ""
read -p "Enter choice [1-6]: " choice

case $choice in
    1)
        echo -e "${GREEN}Starting SonarQube server...${NC}"
        docker-compose --profile sonar up -d sonarqube sonarqube-db
        echo -e "${YELLOW}Waiting for SonarQube to be ready (this may take 2-3 minutes)...${NC}"

        # Wait for SonarQube to be ready
        until $(curl --output /dev/null --silent --head --fail http://localhost:9090); do
            printf '.'
            sleep 5
        done

        echo ""
        echo -e "${GREEN}SonarQube is ready!${NC}"
        echo -e "${GREEN}Access SonarQube at: http://localhost:9090${NC}"
        echo -e "${YELLOW}Default credentials: admin/admin (change on first login)${NC}"
        ;;

    2)
        echo -e "${GREEN}Running SonarQube analysis...${NC}"
        cd backend

        # First, ensure we have test coverage data
        echo -e "${YELLOW}Building and running tests with coverage...${NC}"
        ./mvnw clean verify

        # Then run SonarQube analysis
        echo -e "${GREEN}Sending analysis to SonarQube...${NC}"
        ./mvnw sonar:sonar

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}Analysis complete! View results at: http://localhost:9090${NC}"
        else
            echo -e "${RED}Analysis failed. Is SonarQube server running on port 9090?${NC}"
            echo -e "${YELLOW}Run option 1 to start the server first.${NC}"
        fi
        ;;

    3)
        echo -e "${GREEN}Running JaCoCo coverage analysis only...${NC}"
        cd backend
        ./mvnw clean verify -Pcoverage-only

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}Coverage analysis complete!${NC}"
            echo -e "${GREEN}View report at: backend/target/site/jacoco/index.html${NC}"

            # Try to open the report
            if [[ "$OSTYPE" == "darwin"* ]]; then
                open target/site/jacoco/index.html
            elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
                xdg-open target/site/jacoco/index.html 2>/dev/null
            fi
        fi
        ;;

    4)
        echo -e "${YELLOW}Stopping SonarQube server...${NC}"
        docker-compose --profile sonar down
        echo -e "${GREEN}SonarQube server stopped.${NC}"
        ;;

    5)
        echo -e "${GREEN}Opening coverage reports...${NC}"
        cd backend

        if [ -f target/site/jacoco/index.html ]; then
            echo "Opening JaCoCo coverage report..."
            if [[ "$OSTYPE" == "darwin"* ]]; then
                open target/site/jacoco/index.html
            elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
                xdg-open target/site/jacoco/index.html 2>/dev/null
            fi
        else
            echo -e "${YELLOW}No coverage report found. Run option 3 to generate it.${NC}"
        fi

        if [ -f target/site/jacoco-merged/index.html ]; then
            echo "Opening merged JaCoCo coverage report..."
            if [[ "$OSTYPE" == "darwin"* ]]; then
                open target/site/jacoco-merged/index.html
            elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
                xdg-open target/site/jacoco-merged/index.html 2>/dev/null
            fi
        fi
        ;;

    6)
        echo -e "${GREEN}Exiting...${NC}"
        exit 0
        ;;

    *)
        echo -e "${RED}Invalid option. Please select 1-6.${NC}"
        ;;
esac