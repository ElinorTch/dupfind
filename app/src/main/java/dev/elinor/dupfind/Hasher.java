package dev.elinor.dupfind;

import java.io.IOException;
import java.nio.file.Path;

public interface Hasher {
    String hash(Path path) throws IOException;
}
