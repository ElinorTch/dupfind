package dev.elinor.dupfind.core.scan;

import dev.elinor.dupfind.core.model.FileEntry;
import dev.elinor.dupfind.core.model.ScanOptions;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public final class FileWalker {

    public List<FileEntry> walk(Path root, ScanOptions options, ScanErrorCollector errors)
            throws IOException {

        List<FileEntry> entries = new ArrayList<>();
        Set<FileVisitOption> visitOptions = options.followSymlinks()
                ? EnumSet.of(FileVisitOption.FOLLOW_LINKS)
                : EnumSet.noneOf(FileVisitOption.class);

        Files.walkFileTree(root, visitOptions, Integer.MAX_VALUE, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && attrs.size() >= options.minSize()) {
                    entries.add(new FileEntry(file, attrs.size()));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                errors.record(file, exc);
                return FileVisitResult.CONTINUE;
            }
        });

        return entries;
    }
}