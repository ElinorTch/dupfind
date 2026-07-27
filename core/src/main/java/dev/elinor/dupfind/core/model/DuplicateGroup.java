package dev.elinor.dupfind.core.model;

import java.util.List;

public record DuplicateGroup(String hash, List<FileEntry> files) {
    public long wastedBytes() {
        return (long) (files.size() - 1) * files.getFirst().size();
    }
}
