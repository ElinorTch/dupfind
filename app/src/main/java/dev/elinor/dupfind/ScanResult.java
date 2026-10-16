package dev.elinor.dupfind;

import java.util.List;

public record ScanResult(List<Group> groups, int filesScanned, List<String> errors) {
    public long reclaimableBytes() {
        return groups.stream().mapToLong(Group::wastedBytes).sum();
    }
}
