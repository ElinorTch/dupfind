package dev.elinor.dupfind.core.model;

import dev.elinor.dupfind.core.hash.Hasher;

import java.nio.file.Path;

public record ScanError(Path path, String message) {
}
