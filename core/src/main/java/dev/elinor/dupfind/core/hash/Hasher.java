package dev.elinor.dupfind.core.hash;

import java.io.IOException;
import java.nio.file.Path;

public interface Hasher {
    String hash(Path root) throws IOException;
}
