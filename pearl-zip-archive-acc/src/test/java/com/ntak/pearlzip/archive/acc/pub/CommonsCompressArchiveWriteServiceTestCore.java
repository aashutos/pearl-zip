/*
 * Copyright © 2024 92AK
 */
package com.ntak.pearlzip.archive.acc.pub;

import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.CompressUtil;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;

public abstract class CommonsCompressArchiveWriteServiceTestCore {

    private static Path tempDirectory;
    private static Path tempFile;
    private static Path anotherTempFile;
    private static FileInfo tfFileInfo;
    private static FileInfo atfFileInfo;

    private ArchiveWriteService service;

    @BeforeAll
    public static void setUpOnce() throws IOException {
        tempDirectory = Files.createTempDirectory("pz-test");
        tempFile = Paths.get(tempDirectory.toString(),"temp-file.txt");
        anotherTempFile = Paths.get(tempDirectory.toString(),"another-temp-file.txt");
        Files.createFile(tempFile);
        Files.createFile(anotherTempFile);
        tfFileInfo = new FileInfo(0, 0, "temp-file.txt", 0,
                                  0, 0, null, null,
                                  null, "", "", 0, "", false, false,
                                  Collections.singletonMap(KEY_FILE_PATH, tempFile.toAbsolutePath().toString()));
        atfFileInfo = new FileInfo(0, 0, "another-temp-file.txt", 0,
                                  0, 0, null, null,
                                  null, "", "", 0, "", false, false,
                                  Collections.singletonMap(KEY_FILE_PATH, tempFile.toAbsolutePath().toString()));
    }

    @BeforeEach
    public void setUp() {
        service = new CommonsCompressArchiveWriteService();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        Files.walk(tempDirectory).filter((f)->!Files.isDirectory(f)).forEach(f-> {
            try {
                Files.deleteIfExists(f);
            } catch(IOException e) {
            }
        });
        Files.walk(tempDirectory).filter(Files::isDirectory).sorted((a,b)->b.toString().length()-a.toString().length()).forEach(f-> {
            try {
                Files.deleteIfExists(f);
            } catch(IOException e) {
            }
        });
        Files.deleteIfExists(tempDirectory);
    }

    /*
        Test cases:
        + Create tar compressor archive (gz, xz, bz2)
        + Create file compressor archive (gz, xz, bz2)
        + Create archive with files (zip, tar)
        + Create archive (zip, tar)
        + Add file to archive (zip, tar)
        + Delete file from archive (zip, tar)
     */

    ///// CREATE TAR COMPRESSOR ARCHIVE /////

    @Test
    @DisplayName("Test: Create a valid tar Gzip archive successfully")
    public void testCreateArchive_ValidGzipArchive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.tar.gz");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(29, bytes.length, "File failed to create in the expected manner");
        // gzip magic number
        Assertions.assertEquals((byte)0x1f, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x8B, bytes[1], "second byte issue");
        // Deflate compression
        Assertions.assertEquals((byte)0x08, bytes[2], "third byte issue");
    }

    @Test
    @DisplayName("Test: Create a valid tar XZ archive successfully")
    public void testCreateArchive_ValidXZArchive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.tar.xz");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(76, bytes.length, "File failed to create in the expected manner");
        // Testing magic number
        Assertions.assertEquals((byte)0xFD, bytes[0], "first byte");
        Assertions.assertEquals('7', bytes[1], "second byte issue");
        Assertions.assertEquals('z', bytes[2], "third byte issue");
        Assertions.assertEquals('X', bytes[3], "fourth byte issue");
        Assertions.assertEquals('Z', bytes[4], "fifth byte issue");
        Assertions.assertEquals((byte)0x00, bytes[5], "sixth byte issue");
    }

    @Test
    @DisplayName("Test: Create a valid tar Bzip archive successfully")
    public void testCreateArchive_ValidBz2Archive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.tar.bz2");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(42, bytes.length, "File failed to create in the expected manner");
        // Bzip magic number
        Assertions.assertEquals((byte)0x42, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x5A, bytes[1], "second byte issue");
        Assertions.assertEquals((byte)0x68, bytes[2], "third byte issue");
    }

    ///// CREATE FILE COMPRESSOR ARCHIVE /////

    @Test
    @DisplayName("Test: Create a valid single file Gzip archive successfully")
    public void testCreateArchive_ValidGzipSingleFileArchive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp-file.txt.gz");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(29, bytes.length, "File failed to create in the expected manner");
        // gzip magic number
        Assertions.assertEquals((byte)0x1f, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x8B, bytes[1], "second byte issue");
        // Deflate compression
        Assertions.assertEquals((byte)0x08, bytes[2], "third byte issue");
    }

    @Test
    @DisplayName("Test: Create a valid single file XZ archive successfully")
    public void testCreateArchive_ValidXZSingleFileArchive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp-file.txt.xz");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(76, bytes.length, "File failed to create in the expected manner");
        // Testing magic number
        Assertions.assertEquals((byte)0xFD, bytes[0], "first byte");
        Assertions.assertEquals('7', bytes[1], "second byte issue");
        Assertions.assertEquals('z', bytes[2], "third byte issue");
        Assertions.assertEquals('X', bytes[3], "fourth byte issue");
        Assertions.assertEquals('Z', bytes[4], "fifth byte issue");
        Assertions.assertEquals((byte)0x00, bytes[5], "sixth byte issue");
    }

    @Test
    @DisplayName("Test: Create a valid single file Bzip archive successfully")
    public void testCreateArchive_ValidBz2SingleFileArchive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp-file.txt.bz2");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(42, bytes.length, "File failed to create in the expected manner");
        // Bzip magic number
        Assertions.assertEquals((byte)0x42, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x5A, bytes[1], "second byte issue");
        Assertions.assertEquals((byte)0x68, bytes[2], "third byte issue");
    }

    ///// CREATE EMPTY ARCHIVE /////

    @Test
    @DisplayName("Test: Create a valid empty Zip archive successfully")
    public void testCreateArchive_ValidZipArchive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.zip");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(22, bytes.length, "File failed to create in the expected manner");
        // Zip magic number (Empty archive)
        Assertions.assertEquals((byte)0x50, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x4b, bytes[1], "second byte issue");
        Assertions.assertEquals((byte)0x05, bytes[2], "third byte issue");
        Assertions.assertEquals((byte)0x06, bytes[3], "fourth byte issue");
    }

    @Test
    @DisplayName("Test: Create a valid empty Tar archive successfully")
    public void testCreateArchive_ValidTarArchive_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.tar");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString());
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(1024, bytes.length, "File failed to create in the expected manner");
        // No header initialised until tar entry populated (Memory allocated)
    }

    ///// CREATE ARCHIVE NON EMPTY /////

    @Test
    @DisplayName("Test: Create a valid Zip archive with file successfully")
    public void testCreateArchive_ValidZipArchiveNonEmpty_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.zip");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString(), tfFileInfo);
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(284, bytes.length, "File failed to create in the expected manner");
        // Zip magic number
        Assertions.assertEquals((byte)0x50, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x4b, bytes[1], "second byte issue");
        Assertions.assertEquals((byte)0x03, bytes[2], "third byte issue");
        Assertions.assertEquals((byte)0x04, bytes[3], "fourth byte issue");
    }

    @Test
    @DisplayName("Test: Create a valid empty Tar archive successfully")
    public void testCreateArchive_ValidTarArchiveNonEmpty_Success() throws IOException {
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.tar");
        Files.deleteIfExists(archive);
        service.createArchive(sessionId, archive.toAbsolutePath().toString(), tfFileInfo);
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(2560, bytes.length, "File failed to create in the expected manner");
        // Tar magic number
        Assertions.assertEquals('u', bytes[257], "first byte");
        Assertions.assertEquals('s', bytes[258], "second byte issue");
        Assertions.assertEquals('t', bytes[259], "third byte issue");
        Assertions.assertEquals('a', bytes[260], "fourth byte issue");
        Assertions.assertEquals('r', bytes[261], "fifth byte issue");
    }

    ///// ADD ENTRY TO ARCHIVE /////

    @Test
    @DisplayName("Test: Add a new unique file to a valid Zip archive successfully")
    public void testAddArchive_ValidZipArchiveUniqueFile_Success() throws IOException {
        testCreateArchive_ValidZipArchiveNonEmpty_Success();
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.zip");
        long beforeHash = CompressUtil.crcHashFile(archive.toFile());
        service.addFile(sessionId, archive.toAbsolutePath().toString(), atfFileInfo);
        long afterHash = CompressUtil.crcHashFile(archive.toFile());

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(578, bytes.length, "File failed to create in the expected manner");

        // Zip magic number
        Assertions.assertEquals((byte)0x50, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x4b, bytes[1], "second byte issue");
        Assertions.assertTrue((((byte)0x03 == bytes[2]) && ((byte)0x04 == bytes[3])) || (((byte)0x05 == bytes[2]) && ((byte)0x06 == bytes[3])), "third and/or fourth byte issue");
        Assertions.assertNotEquals(beforeHash, afterHash, "The archive was not updated");
    }

    @Test
    @DisplayName("Test: Add a new unique file to a valid tar archive successfully")
    public void testAddArchive_ValidTarArchiveUniqueFile_Success() throws IOException {
        testCreateArchive_ValidTarArchiveNonEmpty_Success();
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.tar");
        long beforeHash = CompressUtil.crcHashFile(archive.toFile());
        service.addFile(sessionId, archive.toAbsolutePath().toString(), atfFileInfo);
        long afterHash = CompressUtil.crcHashFile(archive.toFile());

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(4096, bytes.length, "File failed to create in the expected manner");
        // Tar magic number
        Assertions.assertEquals('u', bytes[257], "first byte");
        Assertions.assertEquals('s', bytes[258], "second byte issue");
        Assertions.assertEquals('t', bytes[259], "third byte issue");
        Assertions.assertEquals('a', bytes[260], "fourth byte issue");
        Assertions.assertEquals('r', bytes[261], "fifth byte issue");
        Assertions.assertNotEquals(beforeHash, afterHash, "The archive was not updated");
    }

    ///// DELETE ENTRY TO ARCHIVE /////

    @Test
    @DisplayName("Test: Delete existing file from a valid Zip archive successfully")
    public void testDeleteArchive_ValidZipArchiveExistingFile_Success() throws IOException {
        testCreateArchive_ValidZipArchiveNonEmpty_Success();
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.zip");
        long beforeHash = CompressUtil.crcHashFile(archive.toFile());
        service.deleteFile(sessionId, archive.toAbsolutePath().toString(), tfFileInfo);
        long afterHash = CompressUtil.crcHashFile(archive.toFile());

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(22, bytes.length, "File failed to create in the expected manner");
        // Zip magic number (empty archive)
        Assertions.assertEquals((byte)0x50, bytes[0], "first byte");
        Assertions.assertEquals((byte)0x4b, bytes[1], "second byte issue");
        Assertions.assertEquals((byte)0x05, bytes[2], "third byte issue");
        Assertions.assertEquals((byte)0x06, bytes[3], "fourth byte issue");
        Assertions.assertNotEquals(beforeHash, afterHash, "The archive was not updated");
    }

    @Test
    @DisplayName("Test: Delete existing file from a valid tar archive successfully")
    public void testDeleteArchive_ValidTarArchiveExistingFile_Success() throws IOException {
        testCreateArchive_ValidTarArchiveNonEmpty_Success();
        long sessionId = System.currentTimeMillis();
        Path archive = Paths.get(tempDirectory.toString(), "temp.tar");
        long beforeHash = CompressUtil.crcHashFile(archive.toFile());
        service.deleteFile(sessionId, archive.toAbsolutePath().toString(), tfFileInfo);
        long afterHash = CompressUtil.crcHashFile(archive.toFile());

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
        final byte[] bytes = Files.readAllBytes(archive);
        Assertions.assertEquals(1024, bytes.length, "File failed to create in the expected manner");
        Assertions.assertNotEquals(beforeHash, afterHash, "The archive was not updated");
    }
}
