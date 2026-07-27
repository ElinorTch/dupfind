package dev.elinor.dupfind.core.scan;

import dev.elinor.dupfind.core.model.ScanError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScanErrorCollector {

    private final List<ScanError> errors = new ArrayList<>();

    public void record(Path path, IOException cause) {
        errors.add(new ScanError(path, cause.getMessage()));
    }

    public List<ScanError> toList() {
        return List.copyOf(errors);
    }
}
