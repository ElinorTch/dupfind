# Dupfind

A fast command-line tool to find duplicate files by content.

## How it works

dupfind uses a three-pass strategy to avoid unnecessary I/O:

1. **Group by size** — files of different sizes cannot be duplicates.
2. **Partial hash (first 4 KB)** — quickly eliminates non-matching candidates.
3. **Full SHA-256 hash** — confirms exact duplicates among survivors.

## Requirements

- Java 21+

## Build

```bash
./gradlew build
```

To build a native executable (requires GraalVM):

```bash
./gradlew nativeCompile
```

## Usage

```
dupfind [OPTIONS] <directory>
```

### Options

| Flag | Default | Description |
|------|---------|-------------|
| `-m`, `--min-size` | `1` | Skip files smaller than this value (bytes) |
| `-h`, `--help` | | Show help message |
| `-V`, `--version` | | Print version |

### Examples

```bash
# Scan a directory
dupfind ~/Downloads

# Ignore files smaller than 1 MB
dupfind --min-size 1048576 ~/Documents
```

### Output

```
1.2 MB  (3 copies, 2.4 MB wasted)
  /home/user/Downloads/photo.jpg
  /home/user/Pictures/photo.jpg
  /home/user/backup/photo.jpg

2 duplicate groups, 3.1 MB reclaimable, 1482 files scanned.
```

### Exit codes

| Code | Meaning |
|------|---------|
| `0` | No duplicates found |
| `1` | Duplicates found |
| `2` | Invalid argument (e.g. path is not a directory) |
