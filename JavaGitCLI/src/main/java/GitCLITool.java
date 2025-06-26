import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitCLITool {
    private static final String HISTORY_FILE = System.getProperty("user.home") + "/.gitcli_history";
    private static final String ALIAS_FILE = System.getProperty("user.home") + "/.gitcli_aliases";
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.gitcli";

    private CommandHistory history;
    private AliasManager aliasManager;
    private GitManager gitManager;
    private FuzzySearcher fuzzySearcher;
    private Scanner scanner;
    private boolean running;

    public GitCLITool() {
        this.history = new CommandHistory(HISTORY_FILE);
        this.aliasManager = new AliasManager(ALIAS_FILE);
        this.gitManager = new GitManager();
        this.fuzzySearcher = new FuzzySearcher();
        this.scanner = new Scanner(System.in);
        this.running = true;

        // Create config directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Could not create config directory: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        GitCLITool tool = new GitCLITool();
        tool.run();
    }

    public void run() {
        printWelcome();

        while (running) {
            System.out.print("\n" + getCurrentBranch() + " > ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            // Add to history
            history.addCommand(input);

            // Process command
            processCommand(input);
        }

        cleanup();
    }

    private void printWelcome() {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║     Git CLI Workflow Automation      ║");
        System.out.println("║            Version 1.0               ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println("\nType 'help' for available commands");
        System.out.println("Type 'exit' to quit");
    }

    private String getCurrentBranch() {
        try {
            String branch = gitManager.getCurrentBranch();
            return branch != null ? "[" + branch + "]" : "[no-git]";
        } catch (Exception e) {
            return "[no-git]";
        }
    }

    private void processCommand(String input) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        // Check for alias first
        String aliasCommand = aliasManager.getAlias(command);
        if (aliasCommand != null) {
            input = aliasCommand + " " + String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
            parts = input.split("\\s+");
            command = parts[0].toLowerCase();
        }

        try {
            switch (command) {
                case "help":
                    showHelp();
                    break;
                case "exit":
                case "quit":
                    running = false;
                    break;
                case "init":
                    gitManager.initRepository();
                    break;
                case "status":
                case "st":
                    gitManager.showStatus();
                    break;
                case "add":
                    gitManager.addFiles(Arrays.copyOfRange(parts, 1, parts.length));
                    break;
                case "commit":
                case "ci":
                    gitManager.commit(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)));
                    break;
                case "push":
                    gitManager.push();
                    break;
                case "pull":
                    gitManager.pull();
                    break;
                case "branch":
                case "br":
                    handleBranchCommand(Arrays.copyOfRange(parts, 1, parts.length));
                    break;
                case "checkout":
                case "co":
                    gitManager.checkout(parts.length > 1 ? parts[1] : "");
                    break;
                case "log":
                    gitManager.showLog(parts.length > 1 ? Integer.parseInt(parts[1]) : 10);
                    break;
                case "diff":
                    gitManager.showDiff();
                    break;
                case "history":
                case "hist":
                    showHistory();
                    break;
                case "search":
                    handleSearchCommand(Arrays.copyOfRange(parts, 1, parts.length));
                    break;
                case "alias":
                    handleAliasCommand(Arrays.copyOfRange(parts, 1, parts.length));
                    break;
                case "exec":
                    executeSystemCommand(Arrays.copyOfRange(parts, 1, parts.length));
                    break;
                case "pwd":
                    System.out.println(System.getProperty("user.dir"));
                    break;
                case "cd":
                    changeDirectory(parts.length > 1 ? parts[1] : System.getProperty("user.home"));
                    break;
                case "ls":
                    listFiles();
                    break;
                case "clear":
                    clearScreen();
                    break;
                default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Type 'help' for available commands");
            }
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    private void handleBranchCommand(String[] args) {
        if (args.length == 0) {
            gitManager.listBranches();
        } else {
            switch (args[0]) {
                case "-c":
                case "create":
                    if (args.length > 1) {
                        gitManager.createBranch(args[1]);
                    } else {
                        System.out.println("Usage: branch create <branch-name>");
                    }
                    break;
                case "-d":
                case "delete":
                    if (args.length > 1) {
                        gitManager.deleteBranch(args[1]);
                    } else {
                        System.out.println("Usage: branch delete <branch-name>");
                    }
                    break;
                default:
                    gitManager.listBranches();
            }
        }
    }

    private void handleSearchCommand(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: search <query>");
            return;
        }

        String query = String.join(" ", args);
        List<String> commands = history.getCommands();
        List<String> results = fuzzySearcher.search(query, commands);

        if (results.isEmpty()) {
            System.out.println("No matching commands found");
        } else {
            System.out.println("Search results:");
            for (int i = 0; i < Math.min(10, results.size()); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }
        }
    }

    private void handleAliasCommand(String[] args) {
        if (args.length == 0) {
            aliasManager.listAliases();
        } else if (args.length == 1) {
            String alias = aliasManager.getAlias(args[0]);
            if (alias != null) {
                System.out.println(args[0] + " -> " + alias);
            } else {
                System.out.println("Alias not found: " + args[0]);
            }
        } else {
            String aliasName = args[0];
            String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            aliasManager.setAlias(aliasName, command);
            System.out.println("Alias created: " + aliasName + " -> " + command);
        }
    }

    private void executeSystemCommand(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: exec <command>");
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Command exited with code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    private void changeDirectory(String path) {
        try {
            Path newPath = Paths.get(path).toAbsolutePath().normalize();
            if (Files.exists(newPath) && Files.isDirectory(newPath)) {
                System.setProperty("user.dir", newPath.toString());
                System.out.println("Changed directory to: " + newPath);
            } else {
                System.err.println("Directory not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error changing directory: " + e.getMessage());
        }
    }

    private void listFiles() {
        try {
            Files.list(Paths.get(System.getProperty("user.dir")))
                    .sorted()
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) {
                            System.out.println("[DIR]  " + name);
                        } else {
                            System.out.println("[FILE] " + name);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing files: " + e.getMessage());
        }
    }

    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: print empty lines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    private void showHistory() {
        List<String> commands = history.getCommands();
        if (commands.isEmpty()) {
            System.out.println("No command history available");
            return;
        }

        System.out.println("Command History:");
        for (int i = Math.max(0, commands.size() - 20); i < commands.size(); i++) {
            System.out.println((i + 1) + ". " + commands.get(i));
        }
    }

    private void showHelp() {
        System.out.println("\n=== Git CLI Tool Help ===");
        System.out.println("\nGit Commands:");
        System.out.println("  init, init          - Initialise git repository");
        System.out.println("  status, st          - Show git status");
        System.out.println("  add <files>         - Add files to staging");
        System.out.println("  commit <msg>        - Commit changes");
        System.out.println("  push                - Push to remote");
        System.out.println("  pull                - Pull from remote");
        System.out.println("  branch, br          - List branches");
        System.out.println("  branch create <name> - Create new branch");
        System.out.println("  branch delete <name> - Delete branch");
        System.out.println("  checkout <branch>   - Switch branch");
        System.out.println("  log [n]             - Show commit log");
        System.out.println("  diff                - Show differences");

        System.out.println("\nSystem Commands:");
        System.out.println("  exec <command>      - Execute system command");
        System.out.println("  pwd                 - Show current directory");
        System.out.println("  cd <path>           - Change directory");
        System.out.println("  ls                  - List files");
        System.out.println("  clear               - Clear screen");

        System.out.println("\nTool Commands:");
        System.out.println("  history, hist       - Show command history");
        System.out.println("  search <query>      - Fuzzy search history");
        System.out.println("  alias               - List aliases");
        System.out.println("  alias <name> <cmd>  - Create alias");
        System.out.println("  help                - Show this help");
        System.out.println("  exit, quit          - Exit tool");
    }

    private void cleanup() {
        System.out.println("\nSaving configuration...");
        history.save();
        aliasManager.save();
        System.out.println("Goodbye!");
    }
}
