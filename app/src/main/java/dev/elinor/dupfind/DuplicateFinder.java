package dev.elinor.dupfind;

import java.io.IOException;
import java.nio.file.Path;

public interface DuplicateFinder {
    ScanResult find(Path root, long minSize) throws IOException;
}
