package dev.elinor.dupfind;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class DefaultDuplicateFinder implements DuplicateFinder{
    private static final int PARTIAL_BYTES = 4096;

    private final Hasher partialHasher;
    private final Hasher fullHasher;

    public DefaultDuplicateFinder() {
        this(Sha256Hasher.partial(PARTIAL_BYTES), Sha256Hasher.full());
    }

    public DefaultDuplicateFinder(Hasher partialHasher, Hasher fullHasher) {
        this.partialHasher = partialHasher;
        this.fullHasher = fullHasher;
    }

    @Override
    public ScanResult find(Path root, long minSize) throws IOException {
        return new Scan(minSize).run(root);
    }

    /** Single-scan state: counters and errors do not leak across calls. */
    private class Scan {

        private final long minSize;
        private final List<String> errors = new ArrayList<>();
        private int filesScanned = 0;

        Scan(long minSize) {
            this.minSize = minSize;
        }

        ScanResult run(Path root) throws IOException {
            // Step 1: group by size, no file content read
            Map<Long, List<Path>> bySize = walk(root);

            List<Group> groups = new ArrayList<>();

            for (Map.Entry<Long, List<Path>> entry : bySize.entrySet()) {
                long size = entry.getKey();
                List<Path> candidates = entry.getValue();
                if (candidates.size() < 2) continue;

                // Step 2: hash the first 4 KB
                for (List<Path> partial : groupByHash(candidates, partialHasher).values()) {
                    if (partial.size() < 2) continue;

                    // Step 3: full hash on the remaining candidates
                    groupByHash(partial, fullHasher).forEach((hash, files) -> {
                        if (files.size() > 1) {
                            groups.add(new Group(hash, size, files));
                        }
                    });
                }
            }

            groups.sort(Comparator.comparingLong(Group::wastedBytes).reversed());
            return new ScanResult(groups, filesScanned, List.copyOf(errors));
        }

        private Map<Long, List<Path>> walk(Path root) throws IOException {
            Map<Long, List<Path>> bySize = new HashMap<>();

            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && attrs.size() >= minSize) {
                        filesScanned++;
                        bySize.computeIfAbsent(attrs.size(), k -> new ArrayList<>()).add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    errors.add(file + " : " + exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

            return bySize;
        }

        private Map<String, List<Path>> groupByHash(List<Path> paths, Hasher hasher) {
            Map<String, List<Path>> byHash = new LinkedHashMap<>();
            for (Path path : paths) {
                try {
                    byHash.computeIfAbsent(hasher.hash(path), k -> new ArrayList<>()).add(path);
                } catch (IOException e) {
                    errors.add(path + " : " + e.getMessage());
                }
            }
            return byHash;
        }
    }
}
