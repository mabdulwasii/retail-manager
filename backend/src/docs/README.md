# Shop Manager API Documentation

This directory contains the source files for the Shop Manager API documentation, which combines hand-written documentation with auto-generated API specifications.

## Documentation Structure

```
src/docs/asciidoc/
├── index.adoc                 # Main documentation entry point
└── permission-matrix.adoc     # Complete permission matrix
```

## Generating Documentation

### Full Documentation Build

To generate complete documentation with auto-generated API specs:

```bash
# 1. Start the application
./mvnw spring-boot:run

# 2. In a separate terminal, run the full build
./mvnw clean verify
```

This will:
1. Run tests
2. Start the application
3. Generate OpenAPI specification from runtime
4. Convert OpenAPI JSON to AsciiDoc
5. Combine all documentation into HTML

### Manual Documentation Only

To generate just the manual documentation (without auto-generated API specs):

```bash
./mvnw asciidoctor:process-asciidoc
```

## Viewing Documentation

After generation, open:

- **Main Documentation**: `target/generated-docs/index.html`
- **Permission Matrix**: `target/generated-docs/permission-matrix.html`

When deployed, documentation is available at:
- http://localhost:8081/docs/index.html

## What Gets Auto-Generated

The following are automatically extracted from your Spring Boot application:

- **API Endpoints**: All `@RestController` endpoints
- **Request/Response Models**: DTOs and domain objects
- **Parameters**: Query params, path variables, request bodies
- **Authentication**: Security requirements
- **Tags/Groups**: Controller-level groupings

## How It Works

1. **SpringDoc OpenAPI** (`springdoc-openapi-maven-plugin`):
   - Analyzes your running Spring Boot application
   - Reads `@Operation`, `@ApiResponse`, `@Schema` annotations
   - Generates `target/openapi.json`

2. **Swagger2Markup** (`swagger2markup-maven-plugin`):
   - Reads `openapi.json`
   - Converts to AsciiDoc format
   - Outputs to `target/asciidoc/generated/`

3. **AsciiDoctor** (`asciidoctor-maven-plugin`):
   - Processes `src/docs/asciidoc/*.adoc`
   - Includes generated AsciiDoc files
   - Outputs HTML to `target/generated-docs/`

## Updating Documentation

### Adding New Manual Sections

Edit `src/docs/asciidoc/index.adoc` to add new sections:

```asciidoc
== My New Section

Content here...

include::my-new-file.adoc[leveloffset=+1]
```

### Improving API Documentation

Add OpenAPI annotations to your controllers:

```java
@Operation(summary = "Create a product", description = "Creates a new product in the catalog")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Product created"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
})
@PostMapping
public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
    // Implementation
}
```

### Updating Permission Matrix

The permission matrix is automatically generated from:
- `PermissionConstants.java` (defines all permissions)
- Flyway migrations `V12` and `V13` (default role assignments)

To update, edit:
1. `src/docs/asciidoc/permission-matrix.adoc` for descriptions
2. Database migrations for role assignments
3. `PermissionConstants.java` for new permissions

## CI/CD Integration

For CI/CD pipelines, you may want to:

1. **Pre-generate OpenAPI spec** and commit it (avoids starting app in CI)
2. **Use profile** to skip API generation in CI:
   ```bash
   ./mvnw clean verify -DskipOpenApiGeneration=true
   ```

## Troubleshooting

### "Auto-generated API documentation is not available"

This happens when:
- Application is not running during build
- OpenAPI generation failed
- swagger2markup conversion failed

**Solution**: Start the application first, then run `./mvnw verify`

### Plugin Version Issues

Ensure compatible versions:
- `springdoc-openapi-maven-plugin`: 1.4
- `swagger2markup-maven-plugin`: 1.3.3
- `asciidoctor-maven-plugin`: 3.0.0

### Port Conflicts

If port 8081 is in use:
1. Change the port in `application.yml`
2. Update `apiDocsUrl` in `pom.xml`

## Additional Resources

- [AsciiDoc Syntax](https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Swagger2Markup](https://github.com/Swagger2Markup/swagger2markup)
