 package ru.hse.antiplag.filestorageservice.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FileEntityTest {

    @Test
    void testDefaultConstructor() {
        FileEntity fileEntity = new FileEntity();
        assertNull(fileEntity.getId());
        assertNull(fileEntity.getFileName());
        assertNull(fileEntity.getContentType());
        assertNull(fileEntity.getSize());
        assertNull(fileEntity.getUploadTimestamp());
        assertNull(fileEntity.getFilePath());
    }

    @Test
    void testSetters() {
        FileEntity fileEntity = new FileEntity();

        UUID id = UUID.randomUUID();
        fileEntity.setId(id);
        assertEquals(id, fileEntity.getId());

        String fileName = "test.txt";
        fileEntity.setFileName(fileName);
        assertEquals(fileName, fileEntity.getFileName());

        String contentType = "text/plain";
        fileEntity.setContentType(contentType);
        assertEquals(contentType, fileEntity.getContentType());

        Long size = 1337L;
        fileEntity.setSize(size);
        assertEquals(size, fileEntity.getSize());

        LocalDateTime uploadTimestamp = LocalDateTime.now();
        fileEntity.setUploadTimestamp(uploadTimestamp);
        assertEquals(uploadTimestamp, fileEntity.getUploadTimestamp());

        String filePath = "path/to/file";
        fileEntity.setFilePath(filePath);
        assertEquals(filePath, fileEntity.getFilePath());
    }

    @Test
    void testToString() {
        UUID id = UUID.randomUUID();
        String fileName = "test.txt";
        String contentType = "text/plain";
        Long size = 1337L;
        LocalDateTime uploadTimestamp = LocalDateTime.now();
        String filePath = "/path/to/file";

        FileEntity fileEntity = new FileEntity(fileName, contentType, size, uploadTimestamp, filePath);
        fileEntity.setId(id);

        String expectedToString = "FileEntity{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                ", uploadTimestamp=" + uploadTimestamp +
                ", filePath='" + filePath + '\'' +
                '}';
        assertEquals(expectedToString, fileEntity.toString());
    }
}
