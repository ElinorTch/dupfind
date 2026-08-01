package dev.elinor.dupfind.core;

import dev.elinor.dupfind.core.hash.Hasher;
import dev.elinor.dupfind.core.hash.Sha256Hasher;
import dev.elinor.dupfind.core.model.*;
import dev.elinor.dupfind.core.scan.FileWalker;
import dev.elinor.dupfind.core.scan.ScanErrorCollector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class DefaultDuplicateFinder implements DuplicateFinder {

    private static final long PARTIAL_HASH_BYTES = 4 * 1024;

    private final FileWalker walker;
    private final Hasher partialHasher;
    private final Hasher fullHasher;

    public DefaultDuplicateFinder() {
        this(new FileWalker(),
                Sha256Hasher.partial(PARTIAL_HASH_BYTES),
                Sha256Hasher.full());
    }

    // package-private: lets tests inject counting or failing hashers
    DefaultDuplicateFinder(FileWalker walker, Hasher partialHasher, Hasher fullHasher) {
        this.walker = walker;
        this.partialHasher = partialHasher;
        this.fullHasher = fullHasher;
    }

    @Override
    public ScanResult find(Path root, ScanOptions options) throws IOException {
        ScanErrorCollector errors = new ScanErrorCollector();

        // Stage 1 — group by size, no I/O beyond the directory walk
        List<FileEntry> scanned = walker.walk(root, options, errors);
        List<List<FileEntry>> bySize = scanned.stream()
                .collect(Collectors.groupingBy(FileEntry::size))
                .values().stream()
                .filter(group -> group.size() > 1)
                .toList();

        // Stage 2 — cheap discriminator: first 4 KB only
        List<DuplicateGroup> byPartialHash = refine(bySize, partialHasher, errors);

        // Stage 3 — full content hash on the survivors
        List<List<FileEntry>> survivors = byPartialHash.stream()
                .map(DuplicateGroup::files)
                .toList();
        List<DuplicateGroup> confirmed = refine(survivors, fullHasher, errors);

        return new ScanResult(confirmed, scanned.size(), errors.toList());
    }

    private List<DuplicateGroup> refine(List<List<FileEntry>> groups,
                                        Hasher hasher,
                                        ScanErrorCollector errors) {
        List<DuplicateGroup> refined = new ArrayList<>();

        for (List<FileEntry> group : groups) {
            Map<String, List<FileEntry>> byHash = new LinkedHashMap<>();

            for (FileEntry entry : group) {
                try {
                    byHash.computeIfAbsent(hasher.hash(entry.path()), k -> new ArrayList<>())
                            .add(entry);
                } catch (IOException e) {
                    errors.record(entry.path(), e);
                }
            }

            byHash.forEach((hash, files) -> {
                if (files.size() > 1) {
                    refined.add(new DuplicateGroup(hash, files));
                }
            });
        }
        return refined;
    }
}