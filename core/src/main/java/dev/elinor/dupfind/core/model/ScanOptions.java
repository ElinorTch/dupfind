package dev.elinor.dupfind.core.model;

public record ScanOptions(long minSize, boolean followSymlinks) {
    public static ScanOptions defaults() {
        return new ScanOptions(1, false);
    }
}
