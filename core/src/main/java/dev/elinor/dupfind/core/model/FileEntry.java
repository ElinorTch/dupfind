package dev.elinor.dupfind.core.model;

import java.nio.file.Path;

public record FileEntry(Path path, long size) {
}
