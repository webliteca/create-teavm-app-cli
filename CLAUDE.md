# create-teavm-app CLI

CLI tool that scaffolds TeaVM + Cloud Run + Firebase applications.

## Project Structure

```
src/main/java/ca/weblite/tools/createteavmapp/
  CreateTeaVMApp.java       # Picocli @Command entry point — all CLI parameters
  ProjectGenerator.java     # Orchestrates file generation for all modules
  TemplateEngine.java       # {{PLACEHOLDER}} replacement + {{#COND}}/{{^COND}} blocks
  NameUtils.java            # kebab-to-PascalCase, underscore, package path conversions

src/main/resources/templates/
  root/                     # Root project files (pom.xml, .gitignore, dev.sh, etc.)
  mvn/                      # .mvn/ files (settings.xml, maven.config, wrapper props)
  wrapper/                  # mvnw and mvnw.cmd scripts
  common/                   # Common module (pom.xml, package-info.java)
  api/                      # API module (pom.xml, Main.java, HealthResource.java, docker/)
  frontend/                 # Frontend module (pom.xml, App.java, index.html, docker/)
  processor/                # Processor module (pom.xml, Main.java, ProcessResource.java, docker/)
  pubsub-bridge/            # Pub/Sub bridge (bridge.js, package.json)
  workflows/                # GitHub Actions (ci.yml, deploy-staging.yml, deploy-prod.yml)

src/test/java/.../CreateTeaVMAppTest.java  # 24 integration + unit tests
```

## Building

```bash
mvn clean package           # Build uber JAR
mvn test                    # Run tests only
```

The uber JAR is at `target/create-teavm-app-0.1.0.jar`.

## Testing

```bash
mvn test
```

Tests cover:
- Default generation (all files present, correct structure)
- Content verification (correct imports, annotations, API patterns in generated Java)
- Flag combinations (`--no-database`, `--no-auth`, `--object-store`, `--messaging`, `--processor`)
- Skipping modules (`--skip-frontend`, `--skip-workflows`)
- Custom versions, descriptions, group IDs
- `TemplateEngine` and `NameUtils` unit tests

## How It Works

1. `CreateTeaVMApp` parses CLI args via Picocli
2. `ProjectGenerator` builds a variables map (`{{APP_NAME}}`, `{{PACKAGE}}`, etc.) and conditions map (`DATABASE`, `AUTH`, etc.)
3. For each file, it loads a template from `src/main/resources/templates/`, runs it through `TemplateEngine.process()`, and writes the result
4. `TemplateEngine` handles:
   - `{{PLACEHOLDER}}` — simple string replacement
   - `{{#CONDITION}}...{{/CONDITION}}` — include block if condition is true
   - `{{^CONDITION}}...{{/CONDITION}}` — include block if condition is false

## Key Patterns

The generated projects use the teavm-lambda framework. When modifying templates, ensure these patterns are preserved:

- **Main.java**: `GeneratedContainer`, `GeneratedRouter`, `MiddlewareRouter`, `Platform.start(router)`
- **Resources**: `@Path`, `@Component`, `@Singleton`, `@PermitAll`, `@Inject` constructor injection
- **Frontend App.java**: `ReactDOM.createRoot`, `React.wrapComponent`, `Hooks.useState`, `Html.*`
- **API POM**: Two profiles (`teavm` default, `jvm`), both with `annotationProcessorPaths` containing `teavm-lambda-processor` AND `teavm-lambda-core`
- **Frontend POM**: Override `<teavm.version>0.13.1</teavm.version>`, use `teavm-react-core`
- **Root POM**: Full `dependencyManagement` for all teavm-lambda modules

## Adding a New Template Variable

1. Add the variable to `ProjectGenerator.buildVariables()`
2. Use `{{VARIABLE_NAME}}` in the template file
3. Add test assertions in `CreateTeaVMAppTest`

## Adding a New Conditional Feature

1. Add the CLI option in `CreateTeaVMApp.java`
2. Add the condition to `ProjectGenerator.buildConditions()`
3. Use `{{#CONDITION}}...{{/CONDITION}}` in templates
4. Pass the flag through the `ProjectGenerator` constructor
5. Add tests for both enabled and disabled states
