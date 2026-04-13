package ca.weblite.tools.createteavmapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CreateTeaVMAppTest {

    @TempDir
    Path tempDir;

    // ---- Helper methods ----

    private int runCli(String... args) {
        return new CommandLine(new CreateTeaVMApp()).execute(args);
    }

    private String readFile(Path path) throws IOException {
        return Files.readString(path);
    }

    // ---- Test: Default flags (database=true, auth=true) ----

    @Test
    void testDefaultGenerationWithAllDefaults() throws Exception {
        int exitCode = runCli(
                "--name", "my-app",
                "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString()
        );

        assertEquals(0, exitCode);

        Path projectDir = tempDir.resolve("my-app");
        assertTrue(Files.isDirectory(projectDir));

        // Root files
        assertTrue(Files.exists(projectDir.resolve("pom.xml")));
        assertTrue(Files.exists(projectDir.resolve(".gitignore")));
        assertTrue(Files.exists(projectDir.resolve(".tool-versions")));
        assertTrue(Files.exists(projectDir.resolve(".skills-versions")));
        assertTrue(Files.exists(projectDir.resolve("CLAUDE.md")));
        assertTrue(Files.exists(projectDir.resolve("dev.sh")));
        assertTrue(Files.exists(projectDir.resolve("setup-local.sh")));
        assertTrue(Files.exists(projectDir.resolve("docker-compose.yml")));

        // Maven wrapper
        assertTrue(Files.exists(projectDir.resolve("mvnw")));
        assertTrue(Files.exists(projectDir.resolve("mvnw.cmd")));
        assertTrue(Files.exists(projectDir.resolve(".mvn/settings.xml")));
        assertTrue(Files.exists(projectDir.resolve(".mvn/maven.config")));
        assertTrue(Files.exists(projectDir.resolve(".mvn/wrapper/maven-wrapper.properties")));

        // Common module
        assertTrue(Files.isDirectory(projectDir.resolve("my-app-common")));
        assertTrue(Files.exists(projectDir.resolve("my-app-common/pom.xml")));
        assertTrue(Files.exists(projectDir.resolve("my-app-common/src/main/java/ca/weblite/myapp/common/package-info.java")));

        // API module
        assertTrue(Files.isDirectory(projectDir.resolve("my-app-api")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/pom.xml")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/run-tests.sh")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/src/main/java/ca/weblite/myapp/api/Main.java")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/src/main/java/ca/weblite/myapp/api/HealthResource.java")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/docker/Dockerfile")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/docker/package.json")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/docker/server.js")));
        assertTrue(Files.exists(projectDir.resolve("my-app-api/docker/init.sql")));

        // Frontend module (included by default)
        assertTrue(Files.isDirectory(projectDir.resolve("my-app-frontend")));
        assertTrue(Files.exists(projectDir.resolve("my-app-frontend/pom.xml")));
        assertTrue(Files.exists(projectDir.resolve("my-app-frontend/firebase.json")));
        assertTrue(Files.exists(projectDir.resolve("my-app-frontend/src/main/java/ca/weblite/myapp/frontend/App.java")));
        assertTrue(Files.exists(projectDir.resolve("my-app-frontend/src/main/webapp/index.html")));
        assertTrue(Files.exists(projectDir.resolve("my-app-frontend/src/main/config/config.js")));
        assertTrue(Files.exists(projectDir.resolve("my-app-frontend/docker/Dockerfile")));
        assertTrue(Files.exists(projectDir.resolve("my-app-frontend/docker/nginx.conf")));

        // Workflows (included by default)
        assertTrue(Files.exists(projectDir.resolve(".github/workflows/ci.yml")));
        assertTrue(Files.exists(projectDir.resolve(".github/workflows/deploy-staging.yml")));
        assertTrue(Files.exists(projectDir.resolve(".github/workflows/deploy-prod.yml")));
    }

    // ---- Test: Content verification ----

    @Test
    void testRootPomContent() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String pom = readFile(tempDir.resolve("my-app/pom.xml"));

        // Check groupId and artifactId
        assertTrue(pom.contains("<groupId>ca.weblite</groupId>"));
        assertTrue(pom.contains("<artifactId>my-app</artifactId>"));

        // Check modules
        assertTrue(pom.contains("<module>my-app-common</module>"));
        assertTrue(pom.contains("<module>my-app-api</module>"));
        assertTrue(pom.contains("<module>my-app-frontend</module>"));

        // Check dependencyManagement includes all teavm-lambda modules
        assertTrue(pom.contains("teavm-lambda-core"));
        assertTrue(pom.contains("teavm-lambda-processor"));
        assertTrue(pom.contains("teavm-lambda-db-api"));
        assertTrue(pom.contains("teavm-lambda-adapter-cloudrun"));
        assertTrue(pom.contains("teavm-lambda-adapter-httpserver"));
        assertTrue(pom.contains("teavm-lambda-db"));
        assertTrue(pom.contains("teavm-lambda-db-jvm"));
        assertTrue(pom.contains("teavm-lambda-core-js"));
        assertTrue(pom.contains("teavm-lambda-core-jvm"));
        assertTrue(pom.contains("teavm-lambda-auth"));
        assertTrue(pom.contains("teavm-lambda-auth-jvm"));
        assertTrue(pom.contains("teavm-lambda-objectstore"));
        assertTrue(pom.contains("teavm-lambda-s3"));
        assertTrue(pom.contains("teavm-lambda-s3-jvm"));
        assertTrue(pom.contains("teavm-lambda-messagequeue"));
        assertTrue(pom.contains("teavm-lambda-pubsub"));
        assertTrue(pom.contains("teavm-lambda-pubsub-jvm"));
        assertTrue(pom.contains("teavm-lambda-image-api"));
        assertTrue(pom.contains("teavm-lambda-image"));
        assertTrue(pom.contains("teavm-lambda-image-jvm"));
        assertTrue(pom.contains("teavm-lambda-logging"));
        assertTrue(pom.contains("teavm-lambda-compression"));
        assertTrue(pom.contains("teavm-lambda-compression-jvm"));
        assertTrue(pom.contains("teavm-classlib"));
        assertTrue(pom.contains("teavm-jso"));
        assertTrue(pom.contains("teavm-jso-apis"));

        // Check pluginManagement
        assertTrue(pom.contains("maven-compiler-plugin"));
        assertTrue(pom.contains("<version>3.13.0</version>"));
        assertTrue(pom.contains("<release>21</release>"));
    }

    @Test
    void testApiMainJavaContent() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String main = readFile(tempDir.resolve("my-app/my-app-api/src/main/java/ca/weblite/myapp/api/Main.java"));

        // Correct package
        assertTrue(main.contains("package ca.weblite.myapp.api;"));

        // Correct imports
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.Container;"));
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.Platform;"));
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.MiddlewareRouter;"));
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.middleware.CorsMiddleware;"));
        assertTrue(main.contains("import ca.weblite.teavmlambda.generated.GeneratedContainer;"));
        assertTrue(main.contains("import ca.weblite.teavmlambda.generated.GeneratedRouter;"));

        // Database enabled by default
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.db.Database;"));
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.db.DatabaseFactory;"));
        assertTrue(main.contains("DatabaseFactory.create(dbUrl)"));

        // Auth enabled by default
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.auth.JwtValidator;"));
        assertTrue(main.contains("import ca.weblite.teavmlambda.api.auth.JwtValidatorFactory;"));
        assertTrue(main.contains("JwtValidatorFactory.create()"));

        // Core patterns
        assertTrue(main.contains("new GeneratedContainer()"));
        assertTrue(main.contains("new MiddlewareRouter(new GeneratedRouter(container))"));
        assertTrue(main.contains("CorsMiddleware.builder()"));
        assertTrue(main.contains(".allowCredentials(true)"));
        assertTrue(main.contains("Platform.start(router)"));
    }

    @Test
    void testHealthResourceContent() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String hr = readFile(tempDir.resolve("my-app/my-app-api/src/main/java/ca/weblite/myapp/api/HealthResource.java"));

        assertTrue(hr.contains("package ca.weblite.myapp.api;"));
        assertTrue(hr.contains("@Path(\"/health\")"));
        assertTrue(hr.contains("@Component"));
        assertTrue(hr.contains("@Singleton"));
        assertTrue(hr.contains("@PermitAll"));
        assertTrue(hr.contains("@Inject"));
        assertTrue(hr.contains("Response.ok("));

        // Database enabled by default — should have constructor injection with Database
        assertTrue(hr.contains("private final Database database;"));
        assertTrue(hr.contains("database.query(\"SELECT 1\")"));
    }

    @Test
    void testFrontendAppContent() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String app = readFile(tempDir.resolve("my-app/my-app-frontend/src/main/java/ca/weblite/myapp/frontend/App.java"));

        assertTrue(app.contains("package ca.weblite.myapp.frontend;"));
        assertTrue(app.contains("import ca.weblite.teavmreact.core.*;"));
        assertTrue(app.contains("import ca.weblite.teavmreact.hooks.*;"));
        assertTrue(app.contains("import static ca.weblite.teavmreact.html.Html.*;"));
        assertTrue(app.contains("ReactDOM.createRoot"));
        assertTrue(app.contains("React.wrapComponent"));
        assertTrue(app.contains("Hooks.useState"));
    }

    @Test
    void testFrontendPomOverridesTeavmVersion() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String pom = readFile(tempDir.resolve("my-app/my-app-frontend/pom.xml"));

        assertTrue(pom.contains("<teavm.version>0.13.1</teavm.version>"));
        assertTrue(pom.contains("teavm-react-core"));
    }

    @Test
    void testApiPomHasTwoProfiles() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String pom = readFile(tempDir.resolve("my-app/my-app-api/pom.xml"));

        assertTrue(pom.contains("<id>teavm</id>"));
        assertTrue(pom.contains("<id>jvm</id>"));

        // Both profiles have annotationProcessorPaths with both processor AND core
        // Count occurrences of teavm-lambda-processor
        long processorCount = pom.lines()
                .filter(l -> l.contains("teavm-lambda-processor"))
                .count();
        assertTrue(processorCount >= 2, "Both profiles should have teavm-lambda-processor");

        // Check teavm-maven-plugin only in teavm profile
        assertTrue(pom.contains("teavm-maven-plugin"));
    }

    @Test
    void testMvnSettingsAndConfig() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String settings = readFile(tempDir.resolve("my-app/.mvn/settings.xml"));
        assertTrue(settings.contains("${env.GITHUB_TOKEN}"));

        String config = readFile(tempDir.resolve("my-app/.mvn/maven.config"));
        assertTrue(config.contains("-s .mvn/settings.xml"));
    }

    // ---- Test: Skip frontend ----

    @Test
    void testSkipFrontend() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--skip-frontend",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");
        assertFalse(Files.exists(projectDir.resolve("my-app-frontend")));

        String pom = readFile(projectDir.resolve("pom.xml"));
        assertFalse(pom.contains("<module>my-app-frontend</module>"));
    }

    // ---- Test: Skip workflows ----

    @Test
    void testSkipWorkflows() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--skip-workflows",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");
        assertFalse(Files.exists(projectDir.resolve(".github/workflows/ci.yml")));
    }

    // ---- Test: No database ----

    @Test
    void testNoDatabaseFlag() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--no-database",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");

        String main = readFile(projectDir.resolve("my-app-api/src/main/java/ca/weblite/myapp/api/Main.java"));
        assertFalse(main.contains("DatabaseFactory"));
        assertFalse(main.contains("import ca.weblite.teavmlambda.api.db.Database;"));

        String hr = readFile(projectDir.resolve("my-app-api/src/main/java/ca/weblite/myapp/api/HealthResource.java"));
        assertFalse(hr.contains("private final Database database;"));
        assertFalse(hr.contains("database.query"));

        String dc = readFile(projectDir.resolve("docker-compose.yml"));
        assertFalse(dc.contains("POSTGRES_DB"));
    }

    // ---- Test: No auth ----

    @Test
    void testNoAuthFlag() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--no-auth",
                "--output-dir", tempDir.toString());

        String main = readFile(tempDir.resolve("my-app/my-app-api/src/main/java/ca/weblite/myapp/api/Main.java"));
        assertFalse(main.contains("JwtValidator"));
        assertFalse(main.contains("JwtValidatorFactory"));
    }

    // ---- Test: Object store ----

    @Test
    void testObjectStoreFlag() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--object-store",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");

        String dc = readFile(projectDir.resolve("docker-compose.yml"));
        assertTrue(dc.contains("minio"));

        String apiPom = readFile(projectDir.resolve("my-app-api/pom.xml"));
        assertTrue(apiPom.contains("teavm-lambda-objectstore"));

        String apiPkg = readFile(projectDir.resolve("my-app-api/docker/package.json"));
        assertTrue(apiPkg.contains("@aws-sdk/client-s3"));
    }

    // ---- Test: Messaging ----

    @Test
    void testMessagingFlag() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--messaging",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");

        String dc = readFile(projectDir.resolve("docker-compose.yml"));
        assertTrue(dc.contains("pubsub-emulator"));
        assertTrue(dc.contains("pubsub-bridge"));

        String apiPom = readFile(projectDir.resolve("my-app-api/pom.xml"));
        assertTrue(apiPom.contains("teavm-lambda-messagequeue"));

        // Pubsub bridge
        assertTrue(Files.exists(projectDir.resolve("docker/pubsub-bridge/bridge.js")));
        assertTrue(Files.exists(projectDir.resolve("docker/pubsub-bridge/package.json")));
    }

    // ---- Test: Processors ----

    @Test
    void testProcessorGeneration() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--processor", "image-processor",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");

        // Processor module created
        assertTrue(Files.isDirectory(projectDir.resolve("my-app-image-processor")));
        assertTrue(Files.exists(projectDir.resolve("my-app-image-processor/pom.xml")));
        assertTrue(Files.exists(projectDir.resolve("my-app-image-processor/run-tests.sh")));
        assertTrue(Files.exists(projectDir.resolve("my-app-image-processor/docker/Dockerfile")));
        assertTrue(Files.exists(projectDir.resolve("my-app-image-processor/docker/package.json")));
        assertTrue(Files.exists(projectDir.resolve("my-app-image-processor/docker/server.js")));

        Path procSrc = projectDir.resolve("my-app-image-processor/src/main/java/ca/weblite/myapp/imageprocessor");
        assertTrue(Files.exists(procSrc.resolve("Main.java")));
        assertTrue(Files.exists(procSrc.resolve("ProcessResource.java")));
        assertTrue(Files.exists(procSrc.resolve("HealthResource.java")));

        String processResource = readFile(procSrc.resolve("ProcessResource.java"));
        assertTrue(processResource.contains("@Path(\"/process\")"));
        assertTrue(processResource.contains("@POST"));

        // Processor implies messaging
        String dc = readFile(projectDir.resolve("docker-compose.yml"));
        assertTrue(dc.contains("pubsub-emulator"));

        // Root pom has processor module
        String pom = readFile(projectDir.resolve("pom.xml"));
        assertTrue(pom.contains("<module>my-app-image-processor</module>"));
    }

    // ---- Test: Custom description and group ID ----

    @Test
    void testCustomDescriptionAndGroupId() throws Exception {
        runCli("--name", "my-app", "--package", "com.example.myapp",
                "--description", "My test application",
                "--group-id", "com.example",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");

        String pom = readFile(projectDir.resolve("pom.xml"));
        assertTrue(pom.contains("<groupId>com.example</groupId>"));
        assertTrue(pom.contains("My test application"));

        String claude = readFile(projectDir.resolve("CLAUDE.md"));
        assertTrue(claude.contains("My test application"));
    }

    // ---- Test: Custom versions ----

    @Test
    void testCustomVersions() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--teavm-lambda-version", "0.2.0",
                "--teavm-react-version", "0.2.0",
                "--teavm-version", "0.11.0",
                "--output-dir", tempDir.toString());

        Path projectDir = tempDir.resolve("my-app");

        String rootPom = readFile(projectDir.resolve("pom.xml"));
        assertTrue(rootPom.contains("<teavm.lambda.version>0.2.0</teavm.lambda.version>"));
        assertTrue(rootPom.contains("<teavm.version>0.11.0</teavm.version>"));

        // Frontend still uses 0.13.1 regardless
        String frontendPom = readFile(projectDir.resolve("my-app-frontend/pom.xml"));
        assertTrue(frontendPom.contains("<teavm.version>0.13.1</teavm.version>"));

        // React version in frontend pom
        assertTrue(frontendPom.contains("<version>0.2.0</version>"));
    }

    // ---- Test: Derived groupId from package ----

    @Test
    void testDerivedGroupId() throws Exception {
        runCli("--name", "my-app", "--package", "org.example.myapp",
                "--output-dir", tempDir.toString());

        String pom = readFile(tempDir.resolve("my-app/pom.xml"));
        assertTrue(pom.contains("<groupId>org.example</groupId>"));
    }

    // ---- Test: Minimal flags ----

    @Test
    void testMinimalGeneration() throws Exception {
        int exitCode = runCli(
                "--name", "bare-app",
                "--package", "ca.weblite.bareapp",
                "--no-database", "--no-auth",
                "--skip-frontend", "--skip-workflows",
                "--output-dir", tempDir.toString()
        );

        assertEquals(0, exitCode);

        Path projectDir = tempDir.resolve("bare-app");

        // Only common + api modules
        assertTrue(Files.isDirectory(projectDir.resolve("bare-app-common")));
        assertTrue(Files.isDirectory(projectDir.resolve("bare-app-api")));
        assertFalse(Files.exists(projectDir.resolve("bare-app-frontend")));
        assertFalse(Files.exists(projectDir.resolve(".github")));

        // No database/auth in Main.java
        String main = readFile(projectDir.resolve("bare-app-api/src/main/java/ca/weblite/bareapp/api/Main.java"));
        assertFalse(main.contains("Database"));
        assertFalse(main.contains("JwtValidator"));
    }

    // ---- Test: NameUtils ----

    @Test
    void testNameUtils() {
        assertEquals("MyCoolApp", NameUtils.toPascalCase("my-cool-app"));
        assertEquals("App", NameUtils.toPascalCase("app"));
        assertEquals("my_cool_app", NameUtils.toUnderscore("my-cool-app"));
        assertEquals("ca/weblite/myapp", NameUtils.toPackagePath("ca.weblite.myapp"));
        assertEquals("ca.weblite", NameUtils.deriveGroupId("ca.weblite.myapp"));
    }

    // ---- Test: TemplateEngine ----

    @Test
    void testTemplateEngineBasicReplacement() {
        String template = "Hello {{NAME}}, welcome to {{PLACE}}!";
        var vars = java.util.Map.of("NAME", "World", "PLACE", "Earth");
        var conds = java.util.Map.<String, Boolean>of();

        assertEquals("Hello World, welcome to Earth!", TemplateEngine.process(template, vars, conds));
    }

    @Test
    void testTemplateEngineConditionals() {
        String template = "start{{#SHOW}} visible{{/SHOW}} end";

        assertEquals("start visible end",
                TemplateEngine.process(template, java.util.Map.of(), java.util.Map.of("SHOW", true)));
        assertEquals("start end",
                TemplateEngine.process(template, java.util.Map.of(), java.util.Map.of("SHOW", false)));
    }

    @Test
    void testTemplateEngineNegatedConditionals() {
        String template = "start{{^SHOW}} hidden{{/SHOW}} end";

        assertEquals("start end",
                TemplateEngine.process(template, java.util.Map.of(), java.util.Map.of("SHOW", true)));
        assertEquals("start hidden end",
                TemplateEngine.process(template, java.util.Map.of(), java.util.Map.of("SHOW", false)));
    }

    @Test
    void testToolVersionsContent() throws Exception {
        runCli("--name", "my-app", "--package", "ca.weblite.myapp",
                "--output-dir", tempDir.toString());

        String tv = readFile(tempDir.resolve("my-app/.tool-versions"));
        assertTrue(tv.contains("java corretto-21.0.7.6.1"));
        assertTrue(tv.contains("maven 3.9.9"));
        assertTrue(tv.contains("nodejs 22.15.0"));
    }
}
