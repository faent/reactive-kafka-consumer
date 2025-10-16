# Reactive Kafka Consumer

A simple console application demonstrating reactive Kafka consumption using Project Reactor and reactor-kafka. The application prints all consumed Kafka records to the console with Datadog APM tracing which is broken for the second record in concatMap sequence. 

## Prerequisites

Before running this project, ensure you have the following installed:

- **Java 17** or higher
- **Gradle** (or use the included Gradle wrapper)
- **Docker & Docker Compose** for running Kafka locally

Verify your installations:
```bash
java -version    # Should show Java 17+
gradle -version  # Or use ./gradlew
docker-compose --version
```

## Quick Start

Run the provided script:
```bash
chmod +x run.sh
./run.sh
```

This script will:
1. Download the Datadog Java agent if not present (v1.43.0)
2. Build the application using Gradle Shadow plugin
3. Start Kafka and dependencies via Docker Compose
4. Run the reactive Kafka consumer with Datadog instrumentation

## Stopping the Application

Press `Ctrl+C` to stop the application. This will automatically:
- Terminate the Java process
- Shut down Docker Compose services

## Configuration

### Kafka Configuration
Edit `Main.java` to configure:
- Bootstrap servers (default: `localhost:9092`)
- Topic name
- Consumer group ID
- Deserializer settings

### Logging
Logging is configured via `src/main/resources/log4j2.xml`. Default log level is INFO.

### Datadog APM
The Datadog agent version can be changed in `run.sh` by modifying the `DD_AGENT_URL` variable.

## Manual Build & Run

If you prefer to run manually:
```bash
# Build the fat JAR
./gradlew shadowJar

# Start Docker services
docker-compose up -d

# Run with Datadog agent
java -javaagent:./dd-java-agent.jar \
     -jar ./build/libs/reactive-kafka-consumer-1.0-SNAPSHOT-all.jar

# Stop Docker services
docker-compose down
```

## Dependencies

- **reactor-kafka**: 1.3.24
- **log4j2**: 2.25.2
- **Datadog Java Agent**: 1.43.0
