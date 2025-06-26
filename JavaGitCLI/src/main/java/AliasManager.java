import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AliasManager {
    private Map<String, String> aliases;
    private String aliasFile;

    public AliasManager(String aliasFile) {
        this.aliasFile = aliasFile;
        this.aliases = new HashMap<>();
        load();
    }

    public void setAlias(String name, String command) {
        aliases.put(name, command);
    }

    public String getAlias(String name) {
        return aliases.get(name);
    }

    public void listAliases() {
        if (aliases.isEmpty()) {
            System.out.println("No aliases defined");
            return;
        }

        System.out.println("Defined aliases:");
        aliases.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println("  " + entry.getKey() + " -> " + entry.getValue()));
    }

    private void load() {
        try {
            Path path = Paths.get(aliasFile);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        aliases.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load aliases: " + e.getMessage());
        }
    }

    public void save() {
        try {
            List<String> lines = aliases.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toList());
            Files.write(Paths.get(aliasFile), lines);
        } catch (IOException e) {
            System.err.println("Warning: Could not save aliases: " + e.getMessage());
        }
    }
}
