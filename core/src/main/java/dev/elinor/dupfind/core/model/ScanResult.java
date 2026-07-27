package dev.elinor.dupfind.core.model;

import java.util.List;

public record ScanResult(List<DuplicateGroup> groups, int filesScanned, int errors) {
    public long reclaimableBytes() {
        return groups.stream().mapToLong(DuplicateGroup::wastedBytes).sum();
    }
}
