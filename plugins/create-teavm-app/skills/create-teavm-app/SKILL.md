---
name: create-teavm-app
description: "Use this skill whenever the user wants to scaffold, generate, or initialize a new TeaVM application — even if they only mention creating a new Java web app, starting a Cloud Run project, or setting up a teavm-lambda project from scratch. Triggers on: create-teavm-app, scaffold TeaVM app, new teavm-lambda project, generate Java Cloud Run app, create Java serverless app, init TeaVM project, new full-stack Java app, scaffold Cloud Run Firebase app, create-teavm-app CLI, npx create-teavm-app."
globs:
  - "**/pom.xml"
  - "**/.skills-versions"
  - "**/CLAUDE.md"
---

# create-teavm-app

CLI tool that scaffolds a full-stack **TeaVM + Cloud Run + Firebase** application in a single command. Generates a complete multi-module Maven project with API backend, optional frontend SPA, optional processor services, Docker Compose, GitHub Actions workflows, and all configuration files.

**Version**: 0.1.0

## When to Use This Skill

Use `create-teavm-app` when:
- The user asks to create or scaffold a new TeaVM application
- The user wants to start a new Java Cloud Run / Firebase project
- The user wants to generate a project that uses teavm-lambda for the backend and/or teavm-react for the frontend
- The user mentions wanting a new full-stack Java app with serverless deployment

Do **not** use this skill for:
- Adding features to an existing teavm-lambda project (use the `teavm-lambda` skill instead)
- Modifying React components in an existing teavm-react frontend (use the `teavm-react` skill instead)

## Running the CLI

### Via npx (recommended)

```bash
npx create-teavm-app \
  --name my-cool-app \
  --package ca.weblite.mycoolapp \
  --description "My cool application" \
  --database \
  --auth
```

### Via Java JAR

```bash
java -jar create-teavm-app-0.1.0.jar \
  --name my-cool-app \
  --package ca.weblite.mycoolapp
```

## CLI Parameters

### Required

| Parameter | Short | Description |
|-----------|-------|-------------|
| `--name` | `-n` | App name in **kebab-case** (e.g. `my-cool-app`). Becomes Maven artifact prefix, Cloud Run service name, directory name. |
| `--package` | `-p` | Java package (e.g. `ca.weblite.mycoolapp`). Used for all generated Java source files. |

### Optional

| Parameter | Short | Default | Description |
|-----------|-------|---------|-------------|
| `--description` | `-d` | `"A TeaVM application"` | One-line description for POM and CLAUDE.md |
| `--group-id` | `-g` | Derived from package | Maven group ID. If not specified, uses everything before the last `.` segment of the package. |
| `--output-dir` | `-o` | `.` | Where to create the project directory |
| `--database` / `--no-database` | | `true` | Include PostgreSQL database support (Cloud SQL). Adds Database/DatabaseFactory to Main.java, pg to package.json, PostgreSQL to docker-compose. |
| `--auth` / `--no-auth` | | `true` | Include Firebase Auth / JWT validation. Adds JwtValidator/JwtValidatorFactory to Main.java, teavm-lambda-auth deps. |
| `--object-store` | | `false` | Include R2/S3 object storage. Adds MinIO to docker-compose, @aws-sdk/client-s3 to package.json, teavm-lambda-objectstore/s3 deps. |
| `--messaging` | | `false` | Include Pub/Sub messaging. Adds Pub/Sub emulator + bridge to docker-compose, teavm-lambda-messagequeue/pubsub deps. |
| `--processor <name>` | | (none) | Repeatable. Creates a processor service module (implies `--messaging`). Each processor gets its own module with ProcessResource.java at `/process`. |
| `--teavm-lambda-version` | | `0.1.5` | teavm-lambda framework version |
| `--teavm-react-version` | | `0.1.2` | teavm-react library version |
| `--teavm-version` | | `0.13.1` | TeaVM compiler version |
| `--skip-frontend` | | `false` | Skip generating the frontend module |
| `--skip-workflows` | | `false` | Skip generating GitHub Actions workflows |

## Generated Project Structure

For `--name my-app --package ca.weblite.myapp`:

```
my-app/
├── pom.xml                          # Parent POM with dependencyManagement
├── mvnw / mvnw.cmd                  # Maven wrapper
├── .mvn/
│   ├── settings.xml                 # GitHub Packages auth (${env.GITHUB_TOKEN})
│   ├── maven.config                 # -s .mvn/settings.xml
│   └── wrapper/maven-wrapper.properties
├── .tool-versions                   # java corretto-21.0.7.6.1, maven 3.9.9, nodejs 22.15.0
├── .gitignore
├── .skills-versions                 # Skills manifest for install-skill-cli
├── CLAUDE.md                        # AI assistant project documentation
├── dev.sh                           # Build + docker compose up
├── setup-local.sh                   # First-time setup
├── docker-compose.yml               # PostgreSQL, MinIO, Pub/Sub emulator as needed
│
├── my-app-common/                   # Shared models and interfaces
│   ├── pom.xml                      # teavm-lambda-core, teavm-lambda-db-api
│   └── src/.../common/package-info.java
│
├── my-app-api/                      # Backend API service
│   ├── pom.xml                      # teavm & jvm profiles, annotationProcessorPaths
│   ├── run-tests.sh
│   ├── docker/
│   │   ├── Dockerfile               # node:22-alpine
│   │   ├── package.json             # pg, @aws-sdk/client-s3, @google-cloud/pubsub
│   │   ├── server.js                # Loads TeaVM-compiled app.js
│   │   └── init.sql                 # CREATE EXTENSION pgcrypto
│   └── src/.../api/
│       ├── Main.java                # Container, Router, Platform.start()
│       └── HealthResource.java      # GET /health
│
├── my-app-frontend/                 # Frontend SPA (unless --skip-frontend)
│   ├── pom.xml                      # teavm.version=0.13.1, teavm-react-core
│   ├── firebase.json
│   ├── docker/
│   │   ├── Dockerfile               # nginx:alpine
│   │   └── nginx.conf
│   ├── src/main/webapp/index.html
│   ├── src/main/config/config.js    # Firebase config placeholders
│   └── src/.../frontend/App.java    # ReactDOM.createRoot, React.wrapComponent
│
├── my-app-{processor}/              # For each --processor
│   ├── pom.xml
│   ├── docker/
│   └── src/.../{processor}/
│       ├── Main.java
│       ├── ProcessResource.java     # POST /process (Pub/Sub push endpoint)
│       └── HealthResource.java      # GET /health
│
├── docker/pubsub-bridge/            # If --messaging
│   ├── bridge.js                    # Routes Pub/Sub messages to processor endpoints
│   └── package.json
│
└── .github/workflows/               # Unless --skip-workflows
    ├── ci.yml                       # Build + test on push/PR to main
    ├── deploy-staging.yml           # Deploy on push to main
    └── deploy-prod.yml              # Deploy on release
```

## Critical API Patterns in Generated Code

The generated Java files use the **teavm-lambda** framework. These are the correct imports and patterns — never invent alternatives.

### Main.java Pattern

```java
import ca.weblite.teavmlambda.api.Container;
import ca.weblite.teavmlambda.api.Platform;
import ca.weblite.teavmlambda.api.Router;
import ca.weblite.teavmlambda.api.MiddlewareRouter;
import ca.weblite.teavmlambda.api.middleware.CorsMiddleware;
import ca.weblite.teavmlambda.api.db.Database;
import ca.weblite.teavmlambda.api.db.DatabaseFactory;
import ca.weblite.teavmlambda.api.auth.JwtValidator;
import ca.weblite.teavmlambda.api.auth.JwtValidatorFactory;
import ca.weblite.teavmlambda.generated.GeneratedContainer;
import ca.weblite.teavmlambda.generated.GeneratedRouter;

public class Main {
    public static void main(String[] args) {
        String dbUrl = Platform.env("DATABASE_URL", "postgresql://...");
        Container container = new GeneratedContainer();
        container.register(Database.class, DatabaseFactory.create(dbUrl));
        container.register(JwtValidator.class, JwtValidatorFactory.create());
        Router router = new MiddlewareRouter(new GeneratedRouter(container))
                .use(CorsMiddleware.builder().allowCredentials(true).build());
        Platform.start(router);
    }
}
```

**Key rules:**
- Register external deps **before** constructing `GeneratedRouter`
- `GeneratedContainer` and `GeneratedRouter` are compile-time generated by the annotation processor
- `Platform.start(router)` discovers the adapter (Cloud Run / HTTP server) via ServiceLoader

### Resource Pattern

```java
import ca.weblite.teavmlambda.api.Response;
import ca.weblite.teavmlambda.api.annotation.*;
import ca.weblite.teavmlambda.api.db.Database;

@Path("/health")
@Component
@Singleton
@PermitAll
public class HealthResource {
    private final Database database;

    @Inject
    public HealthResource(Database database) {
        this.database = database;
    }

    @GET
    public Response check() {
        database.query("SELECT 1");
        return Response.ok("{\"status\":\"ok\"}")
                .header("Content-Type", "application/json");
    }
}
```

**Key rules:**
- All annotations are in `ca.weblite.teavmlambda.api.annotation` (not javax/jakarta)
- `@Component` + `@Singleton` + `@Inject` constructor = standard DI pattern
- `@PermitAll` or `@RolesAllowed({"role"})` for security
- `Response` is immutable — `.header()` returns a new instance
- Constructor injection only (no field injection)

### Frontend App.java Pattern

```java
import ca.weblite.teavmreact.core.*;
import ca.weblite.teavmreact.hooks.*;
import static ca.weblite.teavmreact.html.Html.*;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLDocument;

public class App {
    public static void main(String[] args) {
        var root = ReactDOM.createRoot(HTMLDocument.current().getElementById("root"));
        JSObject app = React.wrapComponent(App::renderApp, "App");
        root.render(React.createElement(app, null));
    }

    static ReactElement renderApp(JSObject props) {
        StateHandle<Boolean> loggedIn = Hooks.useState(false);
        // loggedIn.getBool(), loggedIn.setBool(true)
        // HTML: div(), nav(), h1(), p(), span()
        // Elements with props: button("text").onClick(e -> ...).className("cls").build()
        return div(
            h1("Welcome").build(),
            p("Hello world").build()
        ).build();
    }
}
```

**Key rules:**
- Entry point: `ReactDOM.createRoot` + `React.wrapComponent` + `root.render`
- Components are static methods returning `ReactElement`
- Use `Hooks.useState`, `Hooks.useEffect`, etc. (same rules as React hooks)
- Frontend POM overrides `<teavm.version>0.13.1</teavm.version>` (different from backend)
- Use `Html.*` static imports for the functional DSL

## POM Structure

### Root POM

The root POM includes:
- `<dependencyManagement>` with **all** teavm-lambda modules (core, processor, db, auth, objectstore, s3, messagequeue, pubsub, image, logging, compression — both JS and JVM variants), plus TeaVM classlib/jso/jso-apis
- `<pluginManagement>` with `maven-compiler-plugin` 3.13.0 targeting Java 21
- GitHub Packages repository for teavm-lambda artifacts

### API Module POM

Two profiles:
- **`teavm`** (default) — compiles Java to JavaScript via `teavm-maven-plugin`, depends on `-js` and `-adapter-cloudrun` modules
- **`jvm`** — standard JVM compilation, depends on `-jvm` and `-adapter-httpserver` modules

Both profiles include `<annotationProcessorPaths>` with **both** `teavm-lambda-processor` AND `teavm-lambda-core`.

### Frontend Module POM

- Overrides `<teavm.version>0.13.1</teavm.version>` in `<properties>`
- Depends on `teavm-react-core` (not `teavm-react`)
- Uses `teavm-maven-plugin` to compile `App.java` to `app.js`

## Common Recipes

### Adding a New API Resource

1. Create `src/main/java/<package>/api/MyResource.java`:
```java
@Path("/my-resource")
@Component
@Singleton
@PermitAll
public class MyResource {
    private final Database database;

    @Inject
    public MyResource(Database database) {
        this.database = database;
    }

    @GET
    public Response list() {
        DbResult result = database.query("SELECT * FROM my_table");
        return Response.ok(result.toJsonArray())
                .header("Content-Type", "application/json");
    }

    @POST
    public Response create(@Body String body) {
        // Parse with JsonReader, validate, insert
        return Response.status(201).body("{\"status\":\"created\"}")
                .header("Content-Type", "application/json");
    }
}
```
2. The annotation processor auto-discovers it — no manual registration needed.

### Adding a New Frontend Page

1. Add a new static render method in `App.java` or a new component class
2. Wire it into the hash router in `renderApp()`
3. Rebuild: `./mvnw -pl <app>-frontend package`

### Adding a Processor Module

Re-run the CLI with `--processor <name>` or manually create the module following the processor template structure. Each processor gets:
- Its own Maven module with teavm + jvm profiles
- `ProcessResource.java` at `@Path("/process")` with `@POST`
- Docker configuration for Cloud Run deployment
- Pub/Sub subscription wired in `docker/pubsub-bridge/bridge.js`

## Build Commands

```bash
./mvnw clean package              # Build all modules
./mvnw -pl <app>-api package -Pteavm   # Build API with TeaVM (Java → JS)
./mvnw -pl <app>-api package -Pjvm     # Build API with JVM (for testing)
./mvnw -pl <app>-frontend package      # Build frontend
./setup-local.sh                        # First-time Docker setup
./dev.sh                                # Build + start local dev environment
```

## Gotchas

1. **No reflection.** teavm-lambda uses compile-time code generation. Do not use Jackson, Gson, or any reflection-based library. Use `JsonBuilder` and `JsonReader`.

2. **`@Body String` is the only body binding.** No automatic JSON-to-POJO. Parse the body string manually with `JsonReader.parse(body)`.

3. **PostgreSQL params use `$1`, `$2`** — not `?`. Example: `db.query("SELECT * FROM users WHERE id = $1", userId)`.

4. **Response is immutable.** `.header()` and `.body()` return new instances. Chain them or reassign.

5. **Register deps before `new GeneratedRouter(container)`.** The router resolves all dependencies at construction time.

6. **Frontend and backend both use TeaVM `0.13.1` by default.** The version can be overridden via `--teavm-version`. The frontend pom.xml also declares its own `<teavm.version>` property.

7. **`@Singleton` is teavm-lambda's annotation** — `ca.weblite.teavmlambda.api.annotation.Singleton`, not `javax.inject` or `jakarta.inject`.
