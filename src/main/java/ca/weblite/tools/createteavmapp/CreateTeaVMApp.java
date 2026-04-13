package ca.weblite.tools.createteavmapp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "create-teavm-app",
        mixinStandardHelpOptions = true,
        version = "create-teavm-app 0.1.0",
        description = "Scaffolds a full-stack TeaVM + Cloud Run + Firebase application."
)
public class CreateTeaVMApp implements Callable<Integer> {

    @Option(names = {"-n", "--name"}, required = true,
            description = "App name in kebab-case (e.g. my-cool-app)")
    private String name;

    @Option(names = {"-p", "--package"}, required = true,
            description = "Java package (e.g. ca.weblite.mycoolapp)")
    private String packageName;

    @Option(names = {"-d", "--description"}, defaultValue = "A TeaVM application",
            description = "One-line description for POM and CLAUDE.md")
    private String description;

    @Option(names = {"-g", "--group-id"},
            description = "Maven group ID (derived from package if not specified)")
    private String groupId;

    @Option(names = {"-o", "--output-dir"}, defaultValue = ".",
            description = "Where to create the project directory")
    private Path outputDir;

    @Option(names = "--database", negatable = true, defaultValue = "true", fallbackValue = "true",
            description = "Include PostgreSQL database support (Cloud SQL)")
    private boolean database;

    @Option(names = "--auth", negatable = true, defaultValue = "true", fallbackValue = "true",
            description = "Include Firebase Auth / JWT validation")
    private boolean auth;

    @Option(names = "--object-store", defaultValue = "false",
            description = "Include R2/S3 object storage")
    private boolean objectStore;

    @Option(names = "--messaging", defaultValue = "false",
            description = "Include Pub/Sub messaging")
    private boolean messaging;

    @Option(names = "--processor", arity = "1",
            description = "Processor service name (repeatable, implies --messaging)")
    private String[] processors;

    @Option(names = "--teavm-lambda-version", defaultValue = "0.1.5",
            description = "teavm-lambda version")
    private String teavmLambdaVersion;

    @Option(names = "--teavm-react-version", defaultValue = "0.1.2",
            description = "teavm-react version")
    private String teavmReactVersion;

    @Option(names = "--teavm-version", defaultValue = "0.13.1",
            description = "TeaVM compiler version")
    private String teavmVersion;

    @Option(names = "--skip-frontend", defaultValue = "false",
            description = "Skip generating the frontend module")
    private boolean skipFrontend;

    @Option(names = "--skip-workflows", defaultValue = "false",
            description = "Skip generating GitHub Actions workflows")
    private boolean skipWorkflows;

    @Override
    public Integer call() throws Exception {
        if (processors != null && processors.length > 0) {
            messaging = true;
        }

        if (groupId == null || groupId.isEmpty()) {
            groupId = NameUtils.deriveGroupId(packageName);
        }

        ProjectGenerator generator = new ProjectGenerator(
                name, packageName, description, groupId, outputDir,
                database, auth, objectStore, messaging,
                processors != null ? processors : new String[0],
                teavmLambdaVersion, teavmReactVersion, teavmVersion,
                skipFrontend, skipWorkflows
        );

        generator.generate();

        System.out.println();
        System.out.println("Created " + name + "/");
        System.out.println();
        System.out.println("Next steps:");
        System.out.println("  cd " + name);
        System.out.println("  ./mvnw clean package        # Build the project");
        System.out.println("  ./setup-local.sh             # Set up local Docker services");
        System.out.println("  ./dev.sh                     # Start local dev environment");
        System.out.println();

        return 0;
    }

    public static void main(String[] args) {
        String mode = System.getProperty("jdeploy.mode", "");
        if ("gui".equals(mode)) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "create-teavm-app v0.1.0\n\nThis is a command-line tool.\nRun 'create-teavm-app' in a terminal for usage.",
                    "About create-teavm-app",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
                System.exit(0);
            });
            return;
        }
        int exitCode = new CommandLine(new CreateTeaVMApp()).execute(args);
        System.exit(exitCode);
    }
}
