package dev.elinor.dupfind;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class DuplicateFinderTest {

    @Test
    void skipsFullHashWhenPartialHashesDiffer(@TempDir Path dir) throws IOException {
        byte[] a = new byte[8192];
        byte[] b = new byte[8192];
        b[0] = 1;                       // même taille, premiers octets différents
        Files.write(dir.resolve("a.bin"), a);
        Files.write(dir.resolve("b.bin"), b);

        var fullHasher = new CountingHasher(Sha256Hasher.full());
        var finder = new DefaultDuplicateFinder(Sha256Hasher.partial(4096), fullHasher);

        var result = finder.find(dir, 0);

        assertThat(result.groups()).isEmpty();
        assertThat(fullHasher.calls()).isZero();   // l'étape 3 n'a jamais tourné
    }
}
