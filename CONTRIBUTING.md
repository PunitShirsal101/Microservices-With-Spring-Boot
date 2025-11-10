# Contributing to Microservices-With-Spring-Boot

Thank you for your interest in contributing to this project! We welcome contributions from everyone.

## How to Contribute

### 1. Fork the Repository
- Click the "Fork" button on GitHub
- Clone your fork locally

### 2. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### 3. Make Your Changes
- Follow the existing code style
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass

### 4. Commit Your Changes
```bash
git add .
git commit -m "Add: Brief description of your changes"
```

### 5. Push and Create Pull Request
```bash
git push origin feature/your-feature-name
```
Then create a pull request on GitHub.

## Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker (for running services)

### Building the Project
```bash
# Build all services
mvn clean install

# Run specific service
cd service-name
mvn spring-boot:run
```

### Running Tests
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Code Standards

### Java Code Style
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods small and focused

### Security Considerations
- Never commit sensitive information
- Use environment variables for secrets
- Validate all inputs
- Follow OWASP guidelines

### Testing
- Write unit tests for all business logic
- Write integration tests for API endpoints
- Maintain test coverage above 80%
- Use meaningful test names

## Pull Request Process

1. **Update the README.md** with details of changes if needed
2. **Update the version numbers** in any examples files and the README.md
3. **Follow the PR template** and fill out all required sections
4. **Wait for review** from maintainers
5. **Address feedback** and make necessary changes
6. **Merge** once approved

## Reporting Issues

- Use the appropriate issue template
- Provide detailed steps to reproduce
- Include environment information
- Add screenshots if applicable

## Security Vulnerabilities

- **DO NOT** report security vulnerabilities through public issues
- Use the security vulnerability report template
- Email maintainers directly for sensitive issues

## License

By contributing to this project, you agree that your contributions will be licensed under the same license as the project.

## Questions?

If you have questions about contributing, please:
- Check existing issues and documentation
- Create a discussion or issue
- Contact the maintainers

Thank you for contributing! 🎉