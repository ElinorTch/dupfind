package dev.elinor.dupfind;

import java.io.IOException;
import java.nio.file.Path;

class CountingHasher implements Hasher {

    private final Hasher delegate;
    private int calls = 0;

    CountingHasher(Hasher delegate) {
        this.delegate = delegate;
    }

    @Override
    public String hash(Path path) throws IOException {
        calls++;
        return delegate.hash(path);
    }

    int calls() {
        return calls;
    }
}