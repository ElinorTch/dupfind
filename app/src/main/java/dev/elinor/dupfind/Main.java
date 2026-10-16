package dev.elinor.dupfind;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "dupfind",
        mixinStandardHelpOptions = true,
        version = "dupfind 0.1.0",
        description = "Find duplicate files by content.")
public class Main implements Callable<Integer> {

    @Parameters(index = "0", description = "Directory to scan.")
    Path root;

    @Option(names = {"-m", "--min-size"}, defaultValue = "1",
            description = "Ignore files smaller than this, in bytes (default: ${DEFAULT-VALUE}).")
    long minSize;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        if (!Files.isDirectory(root)) {
            System.err.println("Not a directory: " + root);
            return 2;
        }

        DuplicateFinder finder = new DefaultDuplicateFinder();
        ScanResult result = finder.find(root, minSize);

        printGroups(result);
        printErrors(result);

        return result.groups().isEmpty() ? 0 : 1;
    }

    private void printGroups(ScanResult result) {
        if (result.groups().isEmpty()) {
            System.out.printf("No duplicates found (%d files scanned).%n",
                    result.filesScanned());
            return;
        }

        for (Group group : result.groups()) {
            System.out.printf("%n%s  (%d copies, %s wasted)%n",
                    humanSize(group.size()),
                    group.files().size(),
                    humanSize(group.wastedBytes()));
            for (Path path : group.files()) {
                System.out.println("  " + path);
            }
        }

        System.out.printf("%n%d duplicate groups, %s reclaimable, %d files scanned.%n",
                result.groups().size(),
                humanSize(result.reclaimableBytes()),
                result.filesScanned());
    }

    private void printErrors(ScanResult result) {
        if (result.errors().isEmpty()) {
            return;
        }
        System.err.printf("%n%d files skipped:%n", result.errors().size());
        result.errors().forEach(error -> System.err.println("  " + error));
    }

    static String humanSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String units = "KMGT";
        double value = bytes;
        int index = 0;
        while (value >= 1024 && index < units.length() - 1) {
            value /= 1024;
            index++;
        }
        return String.format("%.1f %sB", value, units.charAt(index - 1));
    }
}