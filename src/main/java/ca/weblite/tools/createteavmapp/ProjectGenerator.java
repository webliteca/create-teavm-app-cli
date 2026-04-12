package ca.weblite.tools.createteavmapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class ProjectGenerator {

    private static final String TEMPLATES = "/templates";

    private final String appName;
    private final String packageName;
    private final String description;
    private final String groupId;
    private final Path outputDir;
    private final boolean database;
    private final boolean auth;
    private final boolean objectStore;
    private final boolean messaging;
    private final String[] processors;
    private final String teavmLambdaVersion;
    private final String teavmReactVersion;
    private final String teavmVersion;
    private final boolean skipFrontend;
    private final boolean skipWorkflows;

    private final String appNameCamel;
    private final String appNameUnderscore;
    private final String packagePath;
    private final Path projectDir;
    private final Map<String, String> variables;
    private final Map<String, Boolean> conditions;

    public ProjectGenerator(
            String appName, String packageName, String description, String groupId,
            Path outputDir, boolean database, boolean auth, boolean objectStore,
            boolean messaging, String[] processors, String teavmLambdaVersion,
            String teavmReactVersion, String teavmVersion,
            boolean skipFrontend, boolean skipWorkflows) {
        this.appName = appName;
        this.packageName = packageName;
        this.description = description;
        this.groupId = groupId;
        this.outputDir = outputDir;
        this.database = database;
        this.auth = auth;
        this.objectStore = objectStore;
        this.messaging = messaging;
        this.processors = processors;
        this.teavmLambdaVersion = teavmLambdaVersion;
        this.teavmReactVersion = teavmReactVersion;
        this.teavmVersion = teavmVersion;
        this.skipFrontend = skipFrontend;
        this.skipWorkflows = skipWorkflows;

        this.appNameCamel = NameUtils.toPascalCase(appName);
        this.appNameUnderscore = NameUtils.toUnderscore(appName);
        this.packagePath = NameUtils.toPackagePath(packageName);
        this.projectDir = outputDir.resolve(appName);

        this.variables = buildVariables();
        this.conditions = buildConditions();
    }

    private Map<String, String> buildVariables() {
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("APP_NAME", appName);
        vars.put("APP_NAME_CAMEL", appNameCamel);
        vars.put("APP_NAME_UNDERSCORE", appNameUnderscore);
        vars.put("PACKAGE", packageName);
        vars.put("PACKAGE_PATH", packagePath);
        vars.put("GROUP_ID", groupId);
        vars.put("DESCRIPTION", description);
        vars.put("TEAVM_LAMBDA_VERSION", teavmLambdaVersion);
        vars.put("TEAVM_REACT_VERSION", teavmReactVersion);
        vars.put("TEAVM_VERSION", teavmVersion);
        vars.put("MODULES", buildModulesXml());
        vars.put("PROCESSOR_MODULES_CI", buildProcessorModulesCi());
        vars.put("PROCESSOR_DEPLOY_JOBS_STAGING", buildProcessorDeployJobsStaging());
        vars.put("PROCESSOR_DEPLOY_JOBS_PROD", buildProcessorDeployJobsProd());
        vars.put("DOCKER_COMPOSE_SERVICES", buildDockerComposeServices());
        vars.put("PROCESSOR_PROFILES", buildProcessorProfilesDevSh());
        return vars;
    }

    private Map<String, Boolean> buildConditions() {
        Map<String, Boolean> conds = new LinkedHashMap<>();
        conds.put("DATABASE", database);
        conds.put("AUTH", auth);
        conds.put("OBJECT_STORE", objectStore);
        conds.put("MESSAGING", messaging);
        conds.put("FRONTEND", !skipFrontend);
        conds.put("WORKFLOWS", !skipWorkflows);
        conds.put("HAS_PROCESSORS", processors.length > 0);
        return conds;
    }

    public void generate() throws IOException {
        Files.createDirectories(projectDir);

        generateRootFiles();
        generateMvnWrapper();
        generateCommonModule();
        generateApiModule();

        if (!skipFrontend) {
            generateFrontendModule();
        }

        for (String processor : processors) {
            generateProcessorModule(processor);
        }

        if (messaging) {
            generatePubsubBridge();
        }

        if (!skipWorkflows) {
            generateWorkflows();
        }
    }

    // ---- Root files ----

    private void generateRootFiles() throws IOException {
        writeTemplate("/root/pom.xml", projectDir.resolve("pom.xml"));
        writeTemplate("/root/gitignore", projectDir.resolve(".gitignore"));
        writeTemplate("/root/tool-versions", projectDir.resolve(".tool-versions"));
        writeTemplate("/root/skills-versions", projectDir.resolve(".skills-versions"));
        writeTemplate("/root/claude-md", projectDir.resolve("CLAUDE.md"));
        writeTemplate("/root/dev-sh", projectDir.resolve("dev.sh"));
        writeTemplate("/root/setup-local-sh", projectDir.resolve("setup-local.sh"));
        writeTemplate("/root/docker-compose-yml", projectDir.resolve("docker-compose.yml"));

        makeExecutable(projectDir.resolve("dev.sh"));
        makeExecutable(projectDir.resolve("setup-local.sh"));
    }

    private void generateMvnWrapper() throws IOException {
        Path mvnDir = projectDir.resolve(".mvn");
        Files.createDirectories(mvnDir.resolve("wrapper"));

        writeTemplate("/mvn/settings.xml", mvnDir.resolve("settings.xml"));
        writeTemplate("/mvn/maven-config", mvnDir.resolve("maven.config"));
        writeTemplate("/mvn/maven-wrapper.properties", mvnDir.resolve("wrapper/maven-wrapper.properties"));
        writeTemplate("/wrapper/mvnw", projectDir.resolve("mvnw"));
        writeTemplate("/wrapper/mvnw.cmd", projectDir.resolve("mvnw.cmd"));

        makeExecutable(projectDir.resolve("mvnw"));
    }

    // ---- Common module ----

    private void generateCommonModule() throws IOException {
        String moduleName = appName + "-common";
        Path moduleDir = projectDir.resolve(moduleName);
        Path srcDir = moduleDir.resolve("src/main/java/" + packagePath + "/common");
        Files.createDirectories(srcDir);

        writeTemplate("/common/pom.xml", moduleDir.resolve("pom.xml"));
        writeTemplate("/common/package-info.java", srcDir.resolve("package-info.java"));
    }

    // ---- API module ----

    private void generateApiModule() throws IOException {
        String moduleName = appName + "-api";
        Path moduleDir = projectDir.resolve(moduleName);
        Path srcDir = moduleDir.resolve("src/main/java/" + packagePath + "/api");
        Path dockerDir = moduleDir.resolve("docker");
        Files.createDirectories(srcDir);
        Files.createDirectories(dockerDir);

        writeTemplate("/api/pom.xml", moduleDir.resolve("pom.xml"));
        writeTemplate("/api/run-tests.sh", moduleDir.resolve("run-tests.sh"));
        writeTemplate("/api/Main.java", srcDir.resolve("Main.java"));
        writeTemplate("/api/HealthResource.java", srcDir.resolve("HealthResource.java"));
        writeTemplate("/api/docker/Dockerfile", dockerDir.resolve("Dockerfile"));
        writeTemplate("/api/docker/package.json", dockerDir.resolve("package.json"));
        writeTemplate("/api/docker/server.js", dockerDir.resolve("server.js"));
        writeTemplate("/api/docker/init.sql", dockerDir.resolve("init.sql"));

        makeExecutable(moduleDir.resolve("run-tests.sh"));
    }

    // ---- Frontend module ----

    private void generateFrontendModule() throws IOException {
        String moduleName = appName + "-frontend";
        Path moduleDir = projectDir.resolve(moduleName);
        Path srcDir = moduleDir.resolve("src/main/java/" + packagePath + "/frontend");
        Path webappDir = moduleDir.resolve("src/main/webapp");
        Path configDir = moduleDir.resolve("src/main/config");
        Path dockerDir = moduleDir.resolve("docker");
        Files.createDirectories(srcDir);
        Files.createDirectories(webappDir);
        Files.createDirectories(configDir);
        Files.createDirectories(dockerDir);

        writeTemplate("/frontend/pom.xml", moduleDir.resolve("pom.xml"));
        writeTemplate("/frontend/firebase.json", moduleDir.resolve("firebase.json"));
        writeTemplate("/frontend/App.java", srcDir.resolve("App.java"));
        writeTemplate("/frontend/index.html", webappDir.resolve("index.html"));
        writeTemplate("/frontend/config.js", configDir.resolve("config.js"));
        writeTemplate("/frontend/docker/Dockerfile", dockerDir.resolve("Dockerfile"));
        writeTemplate("/frontend/docker/nginx.conf", dockerDir.resolve("nginx.conf"));
    }

    // ---- Processor module ----

    private void generateProcessorModule(String processorName) throws IOException {
        String moduleName = appName + "-" + processorName;
        String processorPackageSegment = processorName.replace("-", "");
        Path moduleDir = projectDir.resolve(moduleName);
        Path srcDir = moduleDir.resolve("src/main/java/" + packagePath + "/" + processorPackageSegment);
        Path dockerDir = moduleDir.resolve("docker");
        Files.createDirectories(srcDir);
        Files.createDirectories(dockerDir);

        Map<String, String> procVars = new HashMap<>(variables);
        procVars.put("PROCESSOR_NAME", processorName);
        procVars.put("PROCESSOR_NAME_CAMEL", NameUtils.toPascalCase(processorName));
        procVars.put("PROCESSOR_PACKAGE_SEGMENT", processorPackageSegment);
        procVars.put("PROCESSOR_MODULE_NAME", moduleName);

        writeTemplate("/processor/pom.xml", moduleDir.resolve("pom.xml"), procVars);
        writeTemplate("/processor/run-tests.sh", moduleDir.resolve("run-tests.sh"), procVars);
        writeTemplate("/processor/Main.java", srcDir.resolve("Main.java"), procVars);
        writeTemplate("/processor/ProcessResource.java", srcDir.resolve("ProcessResource.java"), procVars);
        writeTemplate("/processor/HealthResource.java", srcDir.resolve("HealthResource.java"), procVars);
        writeTemplate("/processor/docker/Dockerfile", dockerDir.resolve("Dockerfile"), procVars);
        writeTemplate("/processor/docker/package.json", dockerDir.resolve("package.json"), procVars);
        writeTemplate("/processor/docker/server.js", dockerDir.resolve("server.js"), procVars);

        makeExecutable(moduleDir.resolve("run-tests.sh"));
    }

    // ---- Pub/Sub bridge ----

    private void generatePubsubBridge() throws IOException {
        Path bridgeDir = projectDir.resolve("docker/pubsub-bridge");
        Files.createDirectories(bridgeDir);

        writeTemplate("/pubsub-bridge/bridge.js", bridgeDir.resolve("bridge.js"));
        writeTemplate("/pubsub-bridge/package.json", bridgeDir.resolve("package.json"));
    }

    // ---- GitHub Actions workflows ----

    private void generateWorkflows() throws IOException {
        Path workflowDir = projectDir.resolve(".github/workflows");
        Files.createDirectories(workflowDir);

        writeTemplate("/workflows/ci.yml", workflowDir.resolve("ci.yml"));
        writeTemplate("/workflows/deploy-staging.yml", workflowDir.resolve("deploy-staging.yml"));
        writeTemplate("/workflows/deploy-prod.yml", workflowDir.resolve("deploy-prod.yml"));
    }

    // ---- Template helpers ----

    private void writeTemplate(String templatePath, Path outputPath) throws IOException {
        writeTemplate(templatePath, outputPath, this.variables);
    }

    private void writeTemplate(String templatePath, Path outputPath, Map<String, String> vars) throws IOException {
        String template = TemplateEngine.loadResource(TEMPLATES + templatePath);
        String content = TemplateEngine.process(template, vars, conditions);
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, content);
    }

    private void makeExecutable(Path path) {
        try {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(path, perms);
        } catch (Exception ignored) {
            // Windows or filesystem doesn't support POSIX permissions
        }
    }

    // ---- Dynamic content builders ----

    private String buildModulesXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<module>").append(appName).append("-common</module>\n");
        sb.append("        <module>").append(appName).append("-api</module>");
        if (!skipFrontend) {
            sb.append("\n        <module>").append(appName).append("-frontend</module>");
        }
        for (String proc : processors) {
            sb.append("\n        <module>").append(appName).append("-").append(proc).append("</module>");
        }
        return sb.toString();
    }

    private String buildDockerComposeServices() {
        StringBuilder sb = new StringBuilder();
        for (String proc : processors) {
            String serviceName = proc.replace("-", "_");
            sb.append("\n  ").append(appName).append("-").append(proc).append(":\n");
            sb.append("    build:\n");
            sb.append("      context: ./").append(appName).append("-").append(proc).append("/docker\n");
            sb.append("    ports:\n");
            sb.append("      - \"808").append(processorPort(proc)).append(":8080\"\n");
            sb.append("    environment:\n");
            sb.append("      PORT: \"8080\"\n");
            if (database) {
                sb.append("      DATABASE_URL: \"postgresql://postgres:postgres@db:5432/").append(appNameUnderscore).append("\"\n");
            }
            sb.append("      PUBSUB_EMULATOR_HOST: \"pubsub-emulator:8085\"\n");
            sb.append("    volumes:\n");
            sb.append("      - ./").append(appName).append("-").append(proc).append("/target/generated/js/").append(proc).append(".js:/app/app.js:ro\n");
            if (database) {
                sb.append("    depends_on:\n");
                sb.append("      db:\n");
                sb.append("        condition: service_healthy\n");
            }
        }
        return sb.toString();
    }

    private int processorPort(String proc) {
        // Assign ports starting from 2 for processors
        int idx = 2;
        for (String p : processors) {
            if (p.equals(proc)) return idx;
            idx++;
        }
        return idx;
    }

    private String buildProcessorModulesCi() {
        StringBuilder sb = new StringBuilder();
        for (String proc : processors) {
            sb.append("\n      - name: Build ").append(proc).append("\n");
            sb.append("        run: ./mvnw -B -pl ").append(appName).append("-").append(proc).append(" package -Pteavm");
        }
        return sb.toString();
    }

    private String buildProcessorDeployJobsStaging() {
        return buildProcessorDeployJobs("staging");
    }

    private String buildProcessorDeployJobsProd() {
        return buildProcessorDeployJobs("prod");
    }

    private String buildProcessorDeployJobs(String env) {
        StringBuilder sb = new StringBuilder();
        for (String proc : processors) {
            sb.append("\n\n  deploy-").append(proc).append(":\n");
            sb.append("    name: Deploy ").append(NameUtils.toPascalCase(proc)).append("\n");
            sb.append("    runs-on: ubuntu-latest\n");
            sb.append("    needs: build\n");
            sb.append("    steps:\n");
            sb.append("      - uses: actions/checkout@v4\n");
            sb.append("      - name: Authenticate to Google Cloud\n");
            sb.append("        uses: google-github-actions/auth@v2\n");
            sb.append("        with:\n");
            sb.append("          credentials_json: ${{ secrets.GCP_SA_KEY }}\n");
            sb.append("      - name: Deploy to Cloud Run\n");
            sb.append("        uses: google-github-actions/deploy-cloudrun@v2\n");
            sb.append("        with:\n");
            sb.append("          service: ").append(appName).append("-").append(proc).append("-").append(env).append("\n");
            sb.append("          source: ").append(appName).append("-").append(proc).append("/docker\n");
            sb.append("          region: us-central1");
        }
        return sb.toString();
    }

    private String buildProcessorProfilesDevSh() {
        StringBuilder sb = new StringBuilder();
        for (String proc : processors) {
            sb.append("\necho \"Building ").append(proc).append("...\"\n");
            sb.append("./mvnw -B -pl ").append(appName).append("-").append(proc).append(" package -Pteavm -DskipTests &");
        }
        return sb.toString();
    }
}
