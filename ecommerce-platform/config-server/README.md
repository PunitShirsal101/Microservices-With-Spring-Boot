# Config Server

The Config Server is a Spring Cloud Config Server that provides centralized configuration management for all microservices in the e-commerce platform. It integrates with HashiCorp Vault for secure secret management and supports Git-based configuration repositories.

## Features

- **Centralized Configuration**: Manages configuration for all microservices from a central location
- **Vault Integration**: Secure storage and retrieval of sensitive configuration data (secrets, credentials, certificates)
- **Git Backend**: Version-controlled configuration files stored in Git repositories
- **Composite Configuration**: Supports multiple configuration sources (Vault + Git)
- **Dynamic Refresh**: Runtime configuration updates without service restarts
- **Security**: Basic authentication and encrypted configuration values
- **Service Discovery**: Registered with Eureka for automatic discovery

## Configuration Sources

### 1. HashiCorp Vault
- **Host**: localhost:8200 (configurable via environment)
- **Authentication**: Token-based authentication
- **KV Engine**: Version 2
- **Backend**: `secret/`
- **Token**: Set via `VAULT_TOKEN` environment variable

### 2. Git Repository
- **URI**: Configurable via `spring.cloud.config.server.git.uri`
- **Search Paths**: `{application}` (application-specific configs)
- **Default Label**: `main`

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VAULT_TOKEN` | Vault authentication token | `hvs.CAES...` |
| `CONFIG_SERVER_USERNAME` | Basic auth username | `configuser` |
| `CONFIG_SERVER_PASSWORD` | Basic auth password | `configpass` |
| `KEYSTORE_PASSWORD` | Keystore password for encryption | `changeit` |

## Usage

### Starting the Config Server

```bash
# Build the service
mvn clean package -pl config-server

# Run the service
java -jar config-server/target/config-server-1.0.0.jar
```

### Accessing Configuration

Once running, other services can access their configuration via:

```
http://localhost:8888/{application}/{profile}
```

Example:
```
http://localhost:8888/user-service/default
http://localhost:8888/api-gateway/production
```

### Vault Setup

1. **Install Vault**:
   ```bash
   # Using Docker
   docker run -d --name vault -p 8200:8200 vault:latest
   ```

2. **Initialize Vault**:
   ```bash
   docker exec vault vault operator init
   ```

3. **Unseal Vault** and set the root token as `VAULT_TOKEN`

4. **Store Secrets**:
   ```bash
   # Enable KV v2 secrets engine
   vault secrets enable -path=secret kv-v2

   # Store application secrets
   vault kv put secret/application/database \
     username=myuser \
     password=mypass

   vault kv put secret/user-service database.username=userdb \
     database.password=userpass
   ```

### Configuration Files Structure

```
config-repo/
├── application.yml          # Common configuration
├── application-prod.yml     # Production overrides
├── user-service.yml         # User service config
├── api-gateway.yml          # API Gateway config
└── ...
```

## Security

- **Basic Authentication**: Protected endpoints with configurable credentials
- **Vault Encryption**: Sensitive data stored encrypted in Vault
- **Network Security**: Config server should be deployed in secure network segments
- **Access Control**: Vault policies should restrict secret access per service

## Monitoring

The config server exposes health and metrics endpoints:

- **Health Check**: `http://localhost:8888/actuator/health`
- **Info**: `http://localhost:8888/actuator/info`
- **Metrics**: `http://localhost:8888/actuator/metrics`

## Troubleshooting

### Common Issues

1. **Vault Connection Failed**:
   - Verify Vault is running and accessible
   - Check `VAULT_TOKEN` is correctly set
   - Ensure Vault is unsealed

2. **Configuration Not Found**:
   - Verify Git repository URL and credentials
   - Check configuration file naming conventions
   - Ensure application name matches file names

3. **Authentication Failed**:
   - Verify basic auth credentials
   - Check security configuration

### Logs

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    org.springframework.cloud.config: DEBUG
    org.springframework.vault: DEBUG
```