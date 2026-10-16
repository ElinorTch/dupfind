package dev.elinor.dupfind;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Sha256Hasher implements Hasher{
    private static final int BUFFER_SIZE = 8192;
    private static final long WHOLE_FILE = -1L;

    private final long maxBytes;

    private Sha256Hasher(long maxBytes) {
        this.maxBytes = maxBytes;
    }

    public static Hasher full() {
        return new Sha256Hasher(WHOLE_FILE);
    }

    public static Hasher partial(long maxBytes) {
        return new Sha256Hasher(maxBytes);
    }

    @Override
    public String hash(Path path) throws IOException {
        MessageDigest digest = newDigest();
        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = (maxBytes == WHOLE_FILE) ? Long.MAX_VALUE : maxBytes;

        try (InputStream in = Files.newInputStream(path)) {
            int read;
            while (remaining > 0
                    && (read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                digest.update(buffer, 0, read);
                remaining -= read;
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is mandated by the Java platform", e);
        }
    }
}
