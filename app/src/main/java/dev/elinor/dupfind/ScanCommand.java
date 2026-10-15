package dev.elinor.dupfind;

import dev.elinor.dupfind.core.DefaultDuplicateFinder;
import dev.elinor.dupfind.core.DuplicateFinder;
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
        System.out.printf("scanning %s (min-size=%d)%n", root, minSize);
        return 0;
    }
}
