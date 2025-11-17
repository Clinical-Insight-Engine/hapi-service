# HAPI Service

A Spring Boot microservice that provides RESTful API access to FHIR (Fast Healthcare Interoperability Resources) data. This service acts as a gateway to interact with FHIR servers, enabling easy retrieval of healthcare resources including Patients, Observations, DocumentReferences, and DiagnosticReports.

## Features

- 🔍 **Patient Management**: Search and retrieve patient information by ID or name
- 📊 **Observations**: Fetch patient observations from FHIR servers
- 📄 **Document References**: Access patient document references
- 🏥 **Diagnostic Reports**: Retrieve diagnostic reports for patients
- 📚 **Swagger Documentation**: Interactive API documentation with Swagger UI
- 🔧 **Configurable**: Easy configuration of FHIR server endpoints
- 🛡️ **Error Handling**: Comprehensive error handling with appropriate HTTP status codes
- 📝 **Logging**: Detailed logging for debugging and monitoring

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.7**
- **HAPI FHIR 6.8.0** - FHIR client library
- **SpringDoc OpenAPI 2.3.0** - API documentation
- **Maven** - Build tool
- **SLF4J** - Logging framework

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)
- Access to a FHIR R4 server (default: https://hapi.fhir.org/baseR4/)

## Installation

1. **Clone the repository** (if applicable):
   ```bash
   git clone <repository-url>
   cd hapi-service
   ```

2. **Build the project**:
   ```bash
   ./mvnw clean install
   ```
   Or on Windows:
   ```bash
   mvnw.cmd clean install
   ```

3. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

   Alternatively, you can run the JAR file:
   ```bash
   java -jar target/hapi-service-0.0.1-SNAPSHOT.jar
   ```

## Configuration

The application can be configured via `src/main/resources/application.properties`:

```properties
# Application name
spring.application.name=hapi-service

# FHIR Server Configuration
fhir.server.base-url=https://hapi.fhir.org/baseR4/

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Environment Variables

You can override the FHIR server URL using environment variables:

```bash
export FHIR_SERVER_BASE_URL=http://your-fhir-server:port/fhir
```

Or via command line:

```bash
java -jar target/hapi-service-0.0.1-SNAPSHOT.jar --fhir.server.base-url=http://your-fhir-server:port/fhir
```

## API Endpoints

### Base URL
```
http://localhost:8080
```

### Patient Endpoints

#### Get Patient by ID
```http
GET /api/patients/{id}
```

**Example:**
```bash
curl http://localhost:8080/api/patients/123
```

#### Search Patients by Name
```http
GET /api/patients/search?name={name}
```

**Example:**
```bash
curl "http://localhost:8080/api/patients/search?name=John%20Doe"
```

### Observation Endpoints

#### Get Observations by Patient ID
```http
GET /api/observations/patient/{patientId}
```

**Example:**
```bash
curl http://localhost:8080/api/observations/patient/123
```

### Document Reference Endpoints

#### Get Document References by Patient ID
```http
GET /api/documentreferences/patient/{patientId}
```

**Example:**
```bash
curl http://localhost:8080/api/documentreferences/patient/123
```

### Diagnostic Report Endpoints

#### Get Diagnostic Reports by Patient ID
```http
GET /api/diagnosticreports/patient/{patientId}
```

**Example:**
```bash
curl http://localhost:8080/api/diagnosticreports/patient/123
```

### Health Check

#### Hello World
```http
GET /hello
```

**Example:**
```bash
curl http://localhost:8080/hello
```

## API Documentation

The application includes interactive Swagger UI documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: http://localhost:8080/api-docs.yaml

The Swagger UI provides:
- Complete API documentation
- Interactive endpoint testing
- Request/response schemas
- Example requests

## Response Format

All endpoints return FHIR resources in JSON format. Search endpoints return FHIR Bundles containing multiple resources.

### Success Response (200 OK)
```json
{
  "resourceType": "Patient",
  "id": "123",
  ...
}
```

### Error Response (404 Not Found)
```json
{
  "error": "Patient not found",
  "patientId": "123",
  "message": "..."
}
```

### Error Response (503 Service Unavailable)
```json
{
  "error": "FHIR server connection failed",
  "message": "..."
}
```

## Project Structure

```
hapi-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/cie/hapi_service/
│   │   │       ├── HapiServiceApplication.java      # Main application class
│   │   │       ├── FhirClientConfig.java            # FHIR client configuration
│   │   │       ├── OpenApiConfig.java               # Swagger/OpenAPI configuration
│   │   │       ├── PatientController.java           # Patient endpoints
│   │   │       ├── ObservationController.java       # Observation endpoints
│   │   │       ├── DocumentReferenceController.java # DocumentReference endpoints
│   │   │       ├── DiagnosticReportController.java  # DiagnosticReport endpoints
│   │   │       └── HelloController.java             # Health check endpoint
│   │   └── resources/
│   │       └── application.properties               # Application configuration
│   └── test/
│       └── java/
│           └── com/cie/hapi_service/
│               └── HapiServiceApplicationTests.java
├── pom.xml                                          # Maven dependencies
├── mvnw                                             # Maven wrapper (Unix)
├── mvnw.cmd                                         # Maven wrapper (Windows)
└── README.md                                        # This file
```

## Development

### Running Tests
```bash
./mvnw test
```

### Building for Production
```bash
./mvnw clean package
```

The JAR file will be created in the `target/` directory.

## Error Handling

The application handles various error scenarios:

- **404 Not Found**: Resource not found on the FHIR server
- **503 Service Unavailable**: Cannot connect to the FHIR server
- **500 Internal Server Error**: Unexpected errors

All errors are logged and return appropriate HTTP status codes with descriptive error messages.

## Logging

The application uses SLF4J for logging. Logs include:
- Request information
- FHIR server interactions
- Error details
- Performance metrics

Logging can be configured in `application.properties` or via environment variables.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For issues, questions, or contributions, please open an issue in the repository.

## Acknowledgments

- [HAPI FHIR](https://hapifhir.io/) - FHIR client library
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [SpringDoc OpenAPI](https://springdoc.org/) - API documentation

