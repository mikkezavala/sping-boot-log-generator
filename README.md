# Synthetic Log Generator

A Spring Boot application that generates realistic synthetic log data for building datasets to emulate various system behaviors. The generator supports integration with Ollama for LLM-enhanced log message generation and provides both scheduled automatic generation and on-demand REST API endpoints.

## Features

- **Realistic Log Scenarios**: Generates logs for 10 different scenarios including user authentication, database operations, API requests, error handling, security events, and more
- **Structured Logging**: Uses Spring Boot's structured logging with key-value pairs in JSON format
- **Ollama Integration**: Optional integration with Ollama for AI-generated log messages
- **REST API**: On-demand log generation via HTTP endpoints
- **Configurable Generation**: Adjustable log generation rates and scenarios
- **Multiple Log Levels**: Supports TRACE, DEBUG, INFO, WARN, and ERROR levels with realistic probability distributions
- **Backward Compatibility**: Maintains legacy log generation for existing integrations

## Quick Start

### Prerequisites

- Java 8 or higher
- Maven 3.6+
- (Optional) Ollama running locally for AI-enhanced log generation

### Running the Application

1. **Clone and build the project:**
   ```bash
   mvn clean package
   ```

2. **Run with default configuration:**
   ```bash
   java -jar target/log-generator.jar
   ```

3. **Run with custom configuration:**
   ```bash
   # Set log generation rate (milliseconds)
   export LOG_RATE_IN_MILLISECONDS=500
   
   # Enable Ollama integration
   export OLLAMA_ENABLED=true
   export OLLAMA_BASE_URL=http://localhost:11434
   export OLLAMA_MODEL=llama2
   
   # Set custom tags
   export TAGS="web-server,database,auth-service"
   
   java -jar target/log-generator.jar
   ```

### Docker Deployment

```bash
# Build Docker image
docker build -t synthetic-log-generator .

# Run container
docker run -p 8080:8080 \
  -e LOG_RATE_IN_MILLISECONDS=1000 \
  -e OLLAMA_ENABLED=false \
  synthetic-log-generator
```

### Kubernetes Deployment

```bash
# Deploy using Helm
helm install log-generator ./log-generator \
  --set replicaCount=3 \
  --set config.logRate=1000 \
  --set config.ollamaEnabled=false
```

## Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `LOG_RATE_IN_MILLISECONDS` | 1000 | Rate for synthetic log generation |
| `LEGACY_LOG_RATE_IN_MILLISECONDS` | 5000 | Rate for legacy log generation |
| `ollama.enabled` | false | Enable Ollama integration |
| `ollama.base-url` | http://localhost:11434 | Ollama server URL |
| `ollama.model` | llama2 | Ollama model to use |

### Environment Variables

- `TAGS`: Comma-separated list of tags for legacy logs
- `LOG_RATE_IN_MILLISECONDS`: Override log generation rate
- `OLLAMA_ENABLED`: Enable/disable Ollama integration
- `OLLAMA_BASE_URL`: Ollama server endpoint
- `OLLAMA_MODEL`: Model name for Ollama

## API Endpoints

### Log Generation

- **POST** `/api/logs/generate` - Generate a single synthetic log entry
  - Query params: `scenario` (optional), `level` (optional)
  
- **POST** `/api/logs/generate/batch` - Generate multiple log entries
  - Query params: `count` (default: 10), `scenario` (optional), `level` (optional), `intervalMs` (default: 100)

- **GET** `/api/logs/stream/{scenario}` - Stream logs for a specific scenario
  - Path param: `scenario` (required)
  - Query params: `intervalMs` (default: 1000), `maxLogs` (default: 100)

### Metadata

- **GET** `/api/logs/scenarios` - List available log scenarios
- **GET** `/api/logs/levels` - List available log levels with probabilities
- **GET** `/api/logs/health` - Health check endpoint

### Monitoring

- **GET** `/actuator/health` - Application health
- **GET** `/actuator/prometheus` - Prometheus metrics
- **GET** `/actuator/info` - Application information

## Log Scenarios

The generator supports the following realistic log scenarios:

1. **USER_LOGIN** - Authentication and login events
2. **DATABASE_OPERATION** - Database queries and transactions
3. **API_REQUEST** - HTTP API requests and responses
4. **ERROR_HANDLING** - Application errors and exceptions
5. **SECURITY_EVENT** - Security-related events and alerts
6. **PERFORMANCE_METRIC** - Performance monitoring data
7. **SYSTEM_STARTUP** - Application startup events
8. **CACHE_OPERATION** - Cache operations and metrics
9. **FILE_OPERATION** - File system operations
10. **BUSINESS_LOGIC** - Business process workflows

## Example Usage

### Generate Single Log Entry

```bash
curl -X POST "http://localhost:8080/api/logs/generate"
```

### Generate Batch with Specific Scenario

```bash
curl -X POST "http://localhost:8080/api/logs/generate/batch?count=5&scenario=USER_LOGIN&level=INFO"
```

### Stream Database Operation Logs

```bash
curl "http://localhost:8080/api/logs/stream/DATABASE_OPERATION?intervalMs=500&maxLogs=20"
```

### Get Available Scenarios

```bash
curl "http://localhost:8080/api/logs/scenarios"
```

## Sample Log Output

```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger_name": "com.log.generator.ScheduledTasks",
  "message": "User authentication successful for user_id=user_12345 session_id=sess_abc123",
  "scenario": "USER_LOGIN",
  "class": "UserController",
  "method": "authenticateUser",
  "trace_id": "a1b2c3d4",
  "user_id": "user_12345",
  "session_id": "sess_abc123",
  "counter": 1001,
  "log_type": "synthetic",
  "ip_address": "192.168.1.100",
  "user_agent": "Mozilla/5.0 (compatible)",
  "login_attempts": 1,
  "request_id": "req_xyz789",
  "thread_id": "thread-5"
}
```

## Ollama Integration

To use AI-generated log messages:

1. **Install and start Ollama:**
   ```bash
   # Install Ollama (macOS)
   brew install ollama
   
   # Start Ollama service
   ollama serve
   
   # Pull a model
   ollama pull llama2
   ```

2. **Enable in configuration:**
   ```properties
   ollama.enabled=true
   ollama.base-url=http://localhost:11434
   ollama.model=llama2
   ```

3. **Restart the application** - The generator will now use Ollama for enhanced log messages with fallback to predefined templates if Ollama is unavailable.

## Troubleshooting

### Common Issues

1. **Ollama connection failed**: Ensure Ollama is running and accessible at the configured URL
2. **High memory usage**: Reduce log generation rate or batch sizes
3. **Missing logs**: Check log level configuration and ensure INFO level is enabled

### Monitoring

Monitor the application using:
- Prometheus metrics at `/actuator/prometheus`
- Health checks at `/actuator/health`
- Application logs for generation statistics
