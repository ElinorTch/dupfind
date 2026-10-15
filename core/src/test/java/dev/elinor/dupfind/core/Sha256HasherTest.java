package dev.elinor.dupfind.core;

import dev.elinor.dupfind.core.hash.Hasher;
import dev.elinor.dupfind.core.hash.Sha256Hasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256HasherTest {

    @Test
    void fullHash_sameContentProducesSameHash(@TempDir Path dir) throws IOException {
        Hasher hasher = Sha256Hasher.full();
        Path a = Files.writeString(dir.resolve("a.txt"), "hello world");
        Path b = Files.writeString(dir.resolve("b.txt"), "hello world");

        assertThat(hasher.hash(a)).isEqualTo(hasher.hash(b));
    }

    @Test
    void fullHash_differentContentProducesDifferentHash(@TempDir Path dir) throws IOException {
        Hasher hasher = Sha256Hasher.full();
        Path a = Files.writeString(dir.resolve("a.txt"), "hello");
        Path b = Files.writeString(dir.resolve("b.txt"), "world");

        assertThat(hasher.hash(a)).isNotEqualTo(hasher.hash(b));
    }

    @Test
    void partialHash_onlyReadsUpToLimit(@TempDir Path dir) throws IOException {
        Hasher partial = Sha256Hasher.partial(4);

        // Both files share the first 4 bytes; differ after
        Path a = Files.write(dir.resolve("a.bin"), new byte[]{'A', 'A', 'A', 'A', 'X'});
        Path b = Files.write(dir.resolve("b.bin"), new byte[]{'A', 'A', 'A', 'A', 'Y'});

        assertThat(partial.hash(a)).isEqualTo(partial.hash(b));
    }

    @Test
    void fullHash_emptyFile_returnsKnownHash(@TempDir Path dir) throws IOException {
        Hasher hasher = Sha256Hasher.full();
        Path empty = Files.write(dir.resolve("empty.bin"), new byte[0]);

        // SHA-256 of empty input is well-known
        assertThat(hasher.hash(empty))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    void partialHash_fileShorterThanLimit_hashesEntireFile(@TempDir Path dir) throws IOException {
        Hasher partial = Sha256Hasher.partial(1024);
        Hasher full = Sha256Hasher.full();
        Path file = Files.writeString(dir.resolve("small.txt"), "tiny");

        assertThat(partial.hash(file)).isEqualTo(full.hash(file));
    }
}
