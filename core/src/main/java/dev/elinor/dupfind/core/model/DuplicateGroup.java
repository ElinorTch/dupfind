package dev.elinor.dupfind.core.model;

import java.util.List;

public record DuplicateGroup(String hash, List<FileEntry> files) {
}
