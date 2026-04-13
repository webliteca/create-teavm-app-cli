# create-teavm-app

CLI tool that scaffolds a full-stack **TeaVM + Cloud Run + Firebase** application in a single command.

## Quick Start

```bash
npx create-teavm-app \
  --name my-cool-app \
  --package ca.weblite.mycoolapp \
  --description "My cool application" \
  --database \
  --auth
```

## Installation

Via npm/npx (recommended — jdeploy handles JRE download automatically):

```bash
npx create-teavm-app --name my-app --package com.example.myapp
```

Or build from source:

```bash
mvn clean package
java -jar target/create-teavm-app-0.1.0.jar --name my-app --package com.example.myapp
```

## CLI Parameters

### Required

| Parameter | Short | Description |
|-----------|-------|-------------|
| `--name` | `-n` | App name in kebab-case (e.g. `my-cool-app`) |
| `--package` | `-p` | Java package (e.g. `ca.weblite.mycoolapp`) |

### Optional

| Parameter | Short | Default | Description |
|-----------|-------|---------|-------------|
| `--description` | `-d` | `"A TeaVM application"` | One-line description for POM and CLAUDE.md |
| `--group-id` | `-g` | Derived from package | Maven group ID |
| `--output-dir` | `-o` | `.` | Where to create the project directory |
| `--database` / `--no-database` | | `true` | Include PostgreSQL database support (Cloud SQL) |
| `--auth` / `--no-auth` | | `true` | Include Firebase Auth / JWT validation |
| `--object-store` | | `false` | Include R2/S3 object storage |
| `--messaging` | | `false` | Include Pub/Sub messaging |
| `--processor` | | (none) | Repeatable. Processor service name (implies `--messaging`) |
| `--teavm-lambda-version` | | `0.1.5` | teavm-lambda version |
| `--teavm-react-version` | | `0.1.2` | teavm-react version |
| `--teavm-version` | | `0.13.1` | TeaVM compiler version |
| `--skip-frontend` | | `false` | Skip generating the frontend module |
| `--skip-workflows` | | `false` | Skip generating GitHub Actions workflows |

## Generated Project Structure

For `--name my-app --package ca.weblite.myapp`:

```
my-app/
├── pom.xml                          # Parent POM with dependencyManagement
├── mvnw / mvnw.cmd                  # Maven wrapper
├── .mvn/settings.xml                # GitHub Packages auth
├── .tool-versions                   # asdf: java, maven, nodejs
├── .gitignore
├── CLAUDE.md                        # Project docs for Claude Code
├── dev.sh                           # Local dev script
├── setup-local.sh                   # First-time setup
├── docker-compose.yml               # PostgreSQL, MinIO, Pub/Sub as needed
│
├── my-app-common/                   # Shared models and interfaces
│   ├── pom.xml
│   └── src/.../common/package-info.java
│
├── my-app-api/                      # Backend API service
│   ├── pom.xml                      # teavm & jvm profiles
│   ├── run-tests.sh
│   ├── docker/                      # Dockerfile, server.js, init.sql
│   └── src/.../api/
│       ├── Main.java                # GeneratedContainer, GeneratedRouter, Platform.start()
│       └── HealthResource.java      # @Path("/health") @Component @Singleton @PermitAll
│
├── my-app-frontend/                 # Frontend SPA (unless --skip-frontend)
│   ├── pom.xml                      # teavm.version=0.13.1, teavm-react-core
│   ├── firebase.json
│   ├── docker/                      # Dockerfile, nginx.conf
│   ├── src/main/webapp/index.html
│   └── src/.../frontend/App.java    # ReactDOM.createRoot, React.wrapComponent
│
├── my-app-{processor}/              # For each --processor
│   ├── pom.xml
│   ├── docker/
│   └── src/.../{processor}/
│       ├── Main.java
│       ├── ProcessResource.java     # @Path("/process") @POST
│       └── HealthResource.java
│
├── docker/pubsub-bridge/            # If --messaging
│
└── .github/workflows/               # Unless --skip-workflows
    ├── ci.yml
    ├── deploy-staging.yml
    └── deploy-prod.yml
```

## Examples

Full-featured app with all defaults:

```bash
npx create-teavm-app -n my-app -p ca.weblite.myapp
```

Minimal API-only app:

```bash
npx create-teavm-app \
  --name my-api \
  --package ca.weblite.myapi \
  --no-database --no-auth \
  --skip-frontend --skip-workflows
```

App with object storage and a processor:

```bash
npx create-teavm-app \
  --name media-app \
  --package ca.weblite.mediaapp \
  --object-store \
  --processor image-processor
```

## Development

### Prerequisites

- Java 21
- Maven 3.9+

### Build

```bash
mvn clean package
```

### Test

```bash
mvn test
```

The test suite includes 24 integration tests covering all flag combinations, generated file content verification, and template engine unit tests.

## Tech Stack

- **Java 21** with **Picocli** for CLI argument parsing
- **maven-shade-plugin** for uber JAR packaging
- **jdeploy** for npm distribution (`npx` support)
- Template-based generation with `{{PLACEHOLDER}}` substitution and `{{#CONDITION}}`/`{{^CONDITION}}` conditional blocks
