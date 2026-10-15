package dev.elinor.dupfind.core;

import dev.elinor.dupfind.core.model.ScanOptions;
import dev.elinor.dupfind.core.model.ScanResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDuplicateFinderTest {

    private final DuplicateFinder finder = new DefaultDuplicateFinder();
    private final ScanOptions defaults = ScanOptions.defaults();

    @Test
    void noDuplicates_emptyResult(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("a.txt"), "hello");
        Files.writeString(dir.resolve("b.txt"), "world");

        ScanResult result = finder.find(dir, defaults);

        assertThat(result.groups()).isEmpty();
        assertThat(result.filesScanned()).isEqualTo(2);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void identicalContent_reportedAsDuplicates(@TempDir Path dir) throws IOException {
        String content = "duplicate content";
        Files.writeString(dir.resolve("a.txt"), content);
        Files.writeString(dir.resolve("b.txt"), content);
        Files.writeString(dir.resolve("c.txt"), content);

        ScanResult result = finder.find(dir, defaults);

        assertThat(result.groups()).hasSize(1);
        assertThat(result.groups().getFirst().files()).hasSize(3);
        assertThat(result.reclaimableBytes())
                .isEqualTo(content.getBytes().length * 2L);
    }

    @Test
    void sameSize_differentContent_notDuplicates(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("a.txt"), "aaaaa");
        Files.writeString(dir.resolve("b.txt"), "bbbbb");

        ScanResult result = finder.find(dir, defaults);

        assertThat(result.groups()).isEmpty();
    }

    @Test
    void minSizeFilter_excludesSmallFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("tiny.txt"), "x");
        Files.writeString(dir.resolve("tiny2.txt"), "x");

        ScanResult result = finder.find(dir, new ScanOptions(10, false));

        assertThat(result.groups()).isEmpty();
        assertThat(result.filesScanned()).isZero();
    }

    @Test
    void multipleGroups_eachGroupIndependent(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("a1.txt"), "group-alpha");
        Files.writeString(dir.resolve("a2.txt"), "group-alpha");
        Files.writeString(dir.resolve("b1.txt"), "group-beta-!!");
        Files.writeString(dir.resolve("b2.txt"), "group-beta-!!");

        ScanResult result = finder.find(dir, defaults);

        assertThat(result.groups()).hasSize(2);
        assertThat(result.filesScanned()).isEqualTo(4);
    }

    @Test
    void emptyDirectory_returnsEmptyResult(@TempDir Path dir) throws IOException {
        ScanResult result = finder.find(dir, defaults);

        assertThat(result.groups()).isEmpty();
        assertThat(result.filesScanned()).isZero();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void nestedDirectories_scannedRecursively(@TempDir Path dir) throws IOException {
        Path sub = Files.createDirectory(dir.resolve("sub"));
        String content = "nested duplicate";
        Files.writeString(dir.resolve("root.txt"), content);
        Files.writeString(sub.resolve("nested.txt"), content);

        ScanResult result = finder.find(dir, defaults);

        assertThat(result.groups()).hasSize(1);
        assertThat(result.groups().getFirst().files()).hasSize(2);
    }

    @Test
    void largeFile_partialHashStillCorrectlyDiscriminates(@TempDir Path dir) throws IOException {
        // Two files that differ only after the 4 KB partial-hash window
        byte[] base = new byte[8 * 1024];
        java.util.Arrays.fill(base, (byte) 'A');

        byte[] fileA = base.clone();
        byte[] fileB = base.clone();
        fileB[5 * 1024] = 'B';  // difference beyond the 4 KB boundary

        Files.write(dir.resolve("same_prefix_a.bin"), fileA);
        Files.write(dir.resolve("same_prefix_b.bin"), fileB);

        ScanResult result = finder.find(dir, defaults);

        assertThat(result.groups()).isEmpty();
    }
}
