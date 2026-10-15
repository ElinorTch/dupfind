package dev.elinor.dupfind;

import dev.elinor.dupfind.core.DefaultDuplicateFinder;
import dev.elinor.dupfind.core.DuplicateFinder;
import dev.elinor.dupfind.core.model.DuplicateGroup;
import dev.elinor.dupfind.core.model.ScanError;
import dev.elinor.dupfind.core.model.ScanOptions;
import dev.elinor.dupfind.core.model.ScanResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "scan", description = "Find duplicate files under a directory.")
public class ScanCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Directory to scan.")
    Path root;

    @Option(names = "--min-size", defaultValue = "1",
            description = "Ignore files smaller than this (bytes).")
    long minSize;

    @Option(names = "--follow-symlinks", defaultValue = "false",
            description = "Follow symbolic links during traversal.")
    boolean followSymlinks;

    private final DuplicateFinder finder;

    public ScanCommand() {
        this(new DefaultDuplicateFinder());
    }

    ScanCommand(DuplicateFinder finder) {
        this.finder = finder;
    }

    @Override
    public Integer call() throws Exception {
        ScanOptions options = new ScanOptions(minSize, followSymlinks);
        ScanResult result = finder.find(root, options);

        if (result.groups().isEmpty()) {
            System.out.printf("No duplicates found (%d files scanned).%n", result.filesScanned());
        } else {
            System.out.printf("Found %d duplicate group%s (%s reclaimable) in %d files scanned.%n%n",
                    result.groups().size(),
                    result.groups().size() == 1 ? "" : "s",
                    formatBytes(result.reclaimableBytes()),
                    result.filesScanned());

            for (DuplicateGroup group : result.groups()) {
                System.out.printf("[%s]  %d files × %s = %s wasted%n",
                        group.hash().substring(0, 16),
                        group.files().size(),
                        formatBytes(group.files().getFirst().size()),
                        formatBytes(group.wastedBytes()));
                for (var file : group.files()) {
                    System.out.printf("  %s%n", file.path());
                }
                System.out.println();
            }
        }

        if (!result.errors().isEmpty()) {
            System.err.printf("%d error%s encountered:%n",
                    result.errors().size(),
                    result.errors().size() == 1 ? "" : "s");
            for (ScanError err : result.errors()) {
                System.err.printf("  %s: %s%n", err.path(), err.message());
            }
        }

        return result.groups().isEmpty() ? 0 : 1;
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
