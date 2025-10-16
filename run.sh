#!/bin/bash

DD_AGENT_JAR="dd-java-agent.jar"
DD_AGENT_URL="https://github.com/DataDog/dd-trace-java/releases/download/v1.54.0/dd-java-agent-1.54.0.jar"

cleanup() {
    echo ""
    echo "Shutting down..."
    docker-compose down
    exit 0
}

trap cleanup SIGINT SIGTERM

if [ ! -f "$DD_AGENT_JAR" ]; then
    echo "Datadog agent not found. Downloading..."
    curl -Lo "$DD_AGENT_JAR" "$DD_AGENT_URL"

    if [ $? -eq 0 ]; then
        echo "Successfully downloaded $DD_AGENT_JAR"
    else
        echo "Failed to download Datadog agent"
        exit 1
    fi
else
    echo "$DD_AGENT_JAR already exists, skipping download"
fi

./gradlew shadowJar

docker-compose up -d

export DD_TRACE_128_BIT_TRACEID_GENERATION_ENABLED=false
java -javaagent:./dd-java-agent.jar -jar ./build/libs/reactive-kafka-consumer-1.0-SNAPSHOT-all.jar
