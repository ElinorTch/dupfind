package dev.elinor.dupfind;

import java.nio.file.Path;
import java.util.List;

public record Group(String hash, long size, List<Path> files) {
    public long wastedBytes() {
        return (files.size() - 1L) * size;
    }
}
