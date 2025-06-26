import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class CommandHistory {
    private List<String> commands;
    private String historyFile;
    private static final int MAX_HISTORY = 1000;

    public CommandHistory(String historyFile) {
        this.historyFile = historyFile;
        this.commands = new ArrayList<>();
        load();
    }

    public void addCommand(String command) {
        commands.add(command);
        if (commands.size() > MAX_HISTORY) {
            commands.remove(0);
        }
    }

    public List<String> getCommands() {
        return new ArrayList<>(commands);
    }

    private void load() {
        try {
            Path path = Paths.get(historyFile);
            if (Files.exists(path)) {
                commands = Files.readAllLines(path);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load command history: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.write(Paths.get(historyFile), commands);
        } catch (IOException e) {
            System.err.println("Warning: Could not save command history: " + e.getMessage());
        }
    }
}