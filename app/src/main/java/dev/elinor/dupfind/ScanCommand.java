package dev.elinor.dupfind;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "scan", description = "Find duplicate files under a directory.")
public class ScanCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Directory to scan.")
    Path root;

    @Option(names = "--min-size", defaultValue = "1",
            description = "Ignore files smaller than this (bytes).")
    long minSize;

    @Override public Integer call() throws Exception {
        System.out.printf("scanning %s (min-size=%d)%n", root, minSize);
        return 0;
    }
}