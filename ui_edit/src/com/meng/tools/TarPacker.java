package com.meng.tools;

import java.io.*;
import java.util.*;

/**
 * Pure Java TAR packer/unpacker.
 * Mirrors the logic from helper/tar_packer.cpp in the C++ project.
 * Pack: packs all files in a source directory into a .tar file (flat, no subdir prefix).
 * Unpack: extracts a .tar file into a target directory.
 */
public class TarPacker {

    private static final int BLOCK_SIZE = 512;
    private static final int HEADER_SIZE = 512;

    /**
     * Pack a directory into a tar file (flat structure - no directory prefix in paths).
     *
     * @param sourceDir      Source directory path (e.g., "../themes/theme_sanae")
     * @param outputTarFile  Output tar file path (e.g., "themes/theme_sanae.tar")
     * @return true if successful
     */
    public static boolean packDirectory(String sourceDir, String outputTarFile) {
        File srcDir = new File(sourceDir);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            System.err.println("Source directory does not exist: " + sourceDir);
            return false;
        }

        File[] files = srcDir.listFiles();
        if (files == null || files.length == 0) {
            System.err.println("No files found in directory: " + sourceDir);
            return false;
        }

        // Ensure output directory exists
        File outputFile = new File(outputTarFile);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (RandomAccessFile tarOut = new RandomAccessFile(outputFile, "rw")) {
            // Collect all files (including subdirectories recursively)
            List<File> allFiles = new ArrayList<>();
            collectFiles(srcDir, allFiles);

            for (File f : allFiles) {
                // Get relative path from source directory
                String relativePath = srcDir.toURI().relativize(f.toURI()).getPath();
                // Replace backslashes with forward slashes for tar format
                relativePath = relativePath.replace('\\', '/');

                if (f.isDirectory()) {
                    writeDirHeader(tarOut, relativePath);
                } else {
                    writeFileHeader(tarOut, relativePath, f.length());
                    writeFileData(tarOut, f);
                }
            }

            // Write two zero-filled blocks to mark end of archive
            byte[] endBlock = new byte[BLOCK_SIZE * 2];
            tarOut.write(endBlock);

            System.out.println("Successfully packed '" + sourceDir + "' to " + outputTarFile);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to pack directory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unpack a tar file into a target directory.
     *
     * @param tarFile   Path to the tar file (e.g., "themes/theme_sanae.tar")
     * @param targetDir Target directory to extract to (e.g., "../themes/theme_sanae")
     * @return true if successful
     */
    public static boolean unpackDirectory(String tarFile, String targetDir) {
        File tar = new File(tarFile);
        if (!tar.exists()) {
            System.err.println("Tar file does not exist: " + tarFile);
            return false;
        }

        File target = new File(targetDir);
        if (!target.exists()) {
            target.mkdirs();
        }

        try (RandomAccessFile tarIn = new RandomAccessFile(tar, "r")) {
            byte[] header = new byte[HEADER_SIZE];

            while (true) {
                int bytesRead = tarIn.read(header);
                if (bytesRead < HEADER_SIZE) break;

                // Check for end-of-archive (zeros)
                if (isZeroBlock(header)) break;

                // Parse header
                String fileName = readFileName(header);
                long fileSize = readFileSize(header);
                byte typeFlag = header[156];

                if (fileName == null || fileName.isEmpty()) break;

                // Normalize path
                fileName = fileName.replace('\\', '/');

                File entryFile = new File(target, fileName);

                if (typeFlag == '5') {
                    // Directory
                    entryFile.mkdirs();
                } else {
                    // Regular file
                    File parent = entryFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        long remaining = fileSize;
                        byte[] buf = new byte[8192];
                        while (remaining > 0) {
                            int toRead = (int) Math.min(buf.length, remaining);
                            int read = tarIn.read(buf, 0, toRead);
                            if (read == -1) break;
                            fos.write(buf, 0, read);
                            remaining -= read;
                        }
                    }

                    // Set last modified time (optional)
                    long mtime = readMTime(header);
                    if (mtime > 0) {
                        entryFile.setLastModified(mtime * 1000);
                    }
                }

                // Skip to next 512-byte boundary
                long padding = (BLOCK_SIZE - (fileSize % BLOCK_SIZE)) % BLOCK_SIZE;
                if (padding > 0) {
                    tarIn.skipBytes((int) padding);
                }
            }

            System.out.println("Successfully unpacked " + tarFile + " to " + targetDir);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to unpack tar file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---- Private helper methods ----

    private static void collectFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            fileList.add(f);
            if (f.isDirectory()) {
                collectFiles(f, fileList);
            }
        }
    }

    private static void writeDirHeader(RandomAccessFile out, String name) throws IOException {
        byte[] header = createHeader(name, 0, '5');
        out.write(header);
    }

    private static void writeFileHeader(RandomAccessFile out, String name, long size) throws IOException {
        byte[] header = createHeader(name, size, '0');
        out.write(header);
    }

    private static void writeFileData(RandomAccessFile out, File file) throws IOException {
        byte[] buf = new byte[8192];
        long remaining = file.length();

        try (FileInputStream fis = new FileInputStream(file)) {
            int read;
            while ((read = fis.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }

        // Pad to 512-byte boundary
        long padding = (BLOCK_SIZE - (remaining % BLOCK_SIZE)) % BLOCK_SIZE;
        if (padding > 0) {
            out.write(new byte[(int) padding]);
        }
    }

    private static byte[] createHeader(String name, long size, char typeFlag) {
        byte[] header = new byte[HEADER_SIZE];

        // Name (100 bytes)
        byte[] nameBytes = name.getBytes();
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));

        // Mode (8 bytes) - 0644 in octal
        String mode = "0000644";
        byte[] modeBytes = mode.getBytes();
        System.arraycopy(modeBytes, 0, header, 100, modeBytes.length);

        // UID (8 bytes)
        String uid = "0000000";
        byte[] uidBytes = uid.getBytes();
        System.arraycopy(uidBytes, 0, header, 108, uidBytes.length);

        // GID (8 bytes)
        String gid = "0000000";
        byte[] gidBytes = gid.getBytes();
        System.arraycopy(gidBytes, 0, header, 116, gidBytes.length);

        // Size (12 bytes) - octal
        String sizeStr = String.format("%011o", size);
        byte[] sizeBytes = sizeStr.getBytes();
        System.arraycopy(sizeBytes, 0, header, 124, sizeBytes.length);

        // MTime (12 bytes)
        long mtime = System.currentTimeMillis() / 1000;
        String mtimeStr = String.format("%011o", mtime);
        byte[] mtimeBytes = mtimeStr.getBytes();
        System.arraycopy(mtimeBytes, 0, header, 136, mtimeBytes.length);

        // Checksum (8 bytes) - fill with spaces initially
        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }

        // Type flag
        header[156] = (byte) typeFlag;

        // Magic + Version
        byte[] magic = "ustar".getBytes();
        System.arraycopy(magic, 0, header, 257, magic.length);
        header[263] = '0';
        header[264] = '0';

        // Calculate checksum
        int checksum = 0;
        for (byte b : header) {
            checksum += b & 0xFF;
        }
        String chkStr = String.format("%06o", checksum);
        byte[] chkBytes = chkStr.getBytes();
        System.arraycopy(chkBytes, 0, header, 148, chkBytes.length);
        header[155] = ' ';

        return header;
    }

    /**
     * Tar entry info with content for text files.
     */
    public static class TarEntryInfo {
        public String fileName;
        public long fileSize;
        public boolean isDirectory;
        public boolean isTextFile;
        public String textContent;
        public byte[] imageData;
        public byte[] rawData;  // Raw binary data for all files
        public String fileType;

        public TarEntryInfo(String fileName, long fileSize, boolean isDirectory) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.isDirectory = isDirectory;
            this.textContent = null;
            this.imageData = null;
            // Guess if text file by extension
            String lower = fileName.toLowerCase();
            this.isTextFile = lower.endsWith(".ini") || lower.endsWith(".txt")
                    || lower.endsWith(".json") || lower.endsWith(".xml")
                    || lower.endsWith(".cfg") || lower.endsWith(".conf")
                    || lower.endsWith(".log") || lower.endsWith(".md");
            if (isDirectory) {
                this.fileType = "directory";
            } else if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp")) {
                this.fileType = "image";
            } else if (lower.endsWith(".ttf") || lower.endsWith(".otf") || lower.endsWith(".woff")) {
                this.fileType = "font";
            } else if (this.isTextFile) {
                this.fileType = "text";
            } else {
                this.fileType = "binary";
            }
        }
    }

    /**
     * Read a tar file and return all entries with text content extracted for text files.
     */
    public static List<TarEntryInfo> readTarEntries(String tarFile) {
        List<TarEntryInfo> entries = new ArrayList<>();
        File tar = new File(tarFile);
        if (!tar.exists()) return entries;

        try (RandomAccessFile tarIn = new RandomAccessFile(tar, "r")) {
            byte[] header = new byte[HEADER_SIZE];

            while (true) {
                int bytesRead = tarIn.read(header);
                if (bytesRead < HEADER_SIZE) break;
                if (isZeroBlock(header)) break;

                String fileName = readFileName(header);
                long fileSize = readFileSize(header);
                byte typeFlag = header[156];

                if (fileName == null || fileName.isEmpty()) break;

                fileName = fileName.replace('\\', '/');
                boolean isDir = (typeFlag == '5');
                TarEntryInfo entry = new TarEntryInfo(fileName, fileSize, isDir);

                if (!isDir && fileSize > 0) {
                    byte[] content = new byte[(int) fileSize];
                    tarIn.readFully(content);
                    entry.rawData = content;  // Store raw data for all files
                    if (entry.isTextFile) {
                        entry.textContent = new String(content, "UTF-8");
                    } else if (entry.fileType.equals("image")) {
                        entry.imageData = content;
                    }
                }

                entries.add(entry);

                // Skip padding to next 512-byte boundary
                long padding = (BLOCK_SIZE - (fileSize % BLOCK_SIZE)) % BLOCK_SIZE;
                if (padding > 0) {
                    tarIn.skipBytes((int) padding);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static String readFileName(byte[] header) {
        // Try prefix[155] + name[100] for long paths
        StringBuilder sb = new StringBuilder();
        byte[] prefix = new byte[155];
        System.arraycopy(header, 345, prefix, 0, 155);
        String prefixStr = new String(prefix).trim();
        if (!prefixStr.isEmpty()) {
            sb.append(prefixStr).append("/");
        }
        byte[] name = new byte[100];
        System.arraycopy(header, 0, name, 0, 100);
        sb.append(new String(name).trim());
        return sb.toString();
    }

    private static long readFileSize(byte[] header) {
        byte[] sizeBytes = new byte[12];
        System.arraycopy(header, 124, sizeBytes, 0, 12);
        String sizeStr = new String(sizeBytes).trim();
        if (sizeStr.isEmpty()) return 0;
        try {
            return Long.parseLong(sizeStr, 8);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long readMTime(byte[] header) {
        byte[] mtimeBytes = new byte[12];
        System.arraycopy(header, 136, mtimeBytes, 0, 12);
        String mtimeStr = new String(mtimeBytes).trim();
        if (mtimeStr.isEmpty()) return 0;
        try {
            return Long.parseLong(mtimeStr, 8);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean isZeroBlock(byte[] block) {
        for (byte b : block) {
            if (b != 0) return false;
        }
        return true;
    }

    /**
     * Rebuild a tar file by replacing a specific file entry with new content.
     * The new content is provided as a byte array. The tar file is rewritten.
     *
     * @param tarFile   Path to original tar file
     * @param targetFileName The exact file name (including any relative path) to replace
     * @param newData   The new data to write for that entry
     * @return true if successful
     */
    public static boolean rebuildTarWithReplacedFile(String tarFile, String targetFileName, byte[] newData) {
        // Read all entries
        List<TarEntryInfo> entries = readTarEntries(tarFile);
        boolean found = false;
        for (TarEntryInfo entry : entries) {
            if (entry.fileName.equals(targetFileName)) {
                found = true;
                // This entry will be replaced; we'll write the new data later
                break;
            }
        }
        if (!found) {
            System.err.println("File not found in tar: " + targetFileName);
            return false;
        }

        // Create a temporary file and rewrite all entries, replacing the target
        File tmpFile = new File(tarFile + ".tmp");
        try (RandomAccessFile tarOut = new RandomAccessFile(tmpFile, "rw")) {
            for (TarEntryInfo entry : entries) {
                String name = entry.fileName;
                if (name.equals(targetFileName)) {
                    // Write with new data
                    if (entry.isDirectory) {
                        writeDirHeader(tarOut, name);
                    } else {
                        writeFileHeader(tarOut, name, newData.length);
                        tarOut.write(newData);
                        long padding = (BLOCK_SIZE - (newData.length % BLOCK_SIZE)) % BLOCK_SIZE;
                        if (padding > 0) {
                            tarOut.write(new byte[(int) padding]);
                        }
                    }
                } else {
                    // Write original entry from the existing tar file
                    if (!entry.isDirectory && entry.textContent != null) {
                        byte[] data = entry.textContent.getBytes("UTF-8");
                        writeFileHeader(tarOut, name, data.length);
                        tarOut.write(data);
                        long padding = (BLOCK_SIZE - (data.length % BLOCK_SIZE)) % BLOCK_SIZE;
                        if (padding > 0) {
                            tarOut.write(new byte[(int) padding]);
                        }
                    } else if (!entry.isDirectory && entry.imageData != null) {
                        byte[] data = entry.imageData;
                        writeFileHeader(tarOut, name, data.length);
                        tarOut.write(data);
                        long padding = (BLOCK_SIZE - (data.length % BLOCK_SIZE)) % BLOCK_SIZE;
                        if (padding > 0) {
                            tarOut.write(new byte[(int) padding]);
                        }
                    } else if (entry.isDirectory) {
                        writeDirHeader(tarOut, name);
                    } else {
                        // For binary files without stored data, we cannot rebuild properly.
                        System.err.println("Warning: Skipping entry " + name + " due to missing data. Rebuild may be incomplete.");
                    }
                }
            }
            byte[] endBlock = new byte[BLOCK_SIZE * 2];
            tarOut.write(endBlock);
        } catch (IOException e) {
            System.err.println("Failed to rebuild tar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        File originalTar = new File(tarFile);
        if (!originalTar.delete()) {
            System.err.println("Failed to delete original tar file: " + tarFile);
            return false;
        }
        if (!tmpFile.renameTo(originalTar)) {
            System.err.println("Failed to rename temp file to original: " + tarFile);
            return false;
        }
        return true;
    }
}
