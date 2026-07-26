package dev.elinor.dupfind.core;

import dev.elinor.dupfind.core.model.ScanOptions;
import dev.elinor.dupfind.core.model.ScanResult;

import java.nio.file.Path;

public interface DuplicateFinder {
    ScanResult find(Path root, ScanOptions options);
}
