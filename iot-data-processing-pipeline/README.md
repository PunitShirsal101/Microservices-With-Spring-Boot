# IoT Data Processing Pipeline

## Prerequisites

- Java 17+
- Apache Kafka
- Elasticsearch 8+
- Kibana
- Docker
- Docker Compose

## Setup

1. Ensure Docker and Docker Compose are installed.
2. Start the services using Docker Compose:
   ```bash
   docker-compose up -d
   ```
   This will start Zookeeper, Kafka, Elasticsearch, and Kibana containers.
3. Wait a few minutes for the services to fully start.
4. Create a Kafka topic:
   ```bash
   docker exec kafka kafka-topics --create --topic iot-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
   ```
   Alternatively, if running locally: `kafka-topics --create --topic iot-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1`

## Build and Run

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.iot.pipeline.Main"
```

## Build and Run (Windows)

If you're using Windows (cmd.exe), copy and run the included helper script:

```cmd
run.bat
```

## Sample quick verification (Windows cmd.exe)

```cmd
:: Start docker-compose
docker-compose up -d
:: Wait ~30-60s for services to become healthy
:: Create topic (inside container safe form)
docker exec kafka kafka-topics --create --topic iot-events --bootstrap-server kafka:29092 --partitions 3 --replication-factor 1
:: Build and run the app
mvn clean package -DskipTests
mvn exec:java -Dexec.classpathScope=runtime -Dexec.mainClass="com.iot.pipeline.Main"
:: Check Elasticsearch directly
curl http://localhost:9200/iot-events/_search?q=deviceId:device1&pretty
```
