package ru.hse.antiplag.filestorageservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


import ru.hse.antiplag.filestorageservice.domain.FileEntity;
import ru.hse.antiplag.filestorageservice.repository.FileRepository;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplTest {

  @Mock
  private FileRepository fileRepository;

  @TempDir
  Path tempDir;

  private FileStorageService fileStorageService;

  @BeforeEach
  void setUp() throws IOException {
    fileStorageService = new FileStorageServiceImpl(tempDir.toString(), fileRepository);
  }

  @Test
  void storeFileTest() throws IOException {
    MockMultipartFile multipartFile = new MockMultipartFile(
        "file",
        "test-aboba.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "Aboba, Kek!".getBytes()
    );

    ArgumentCaptor<FileEntity> fileEntityArgumentCaptor = ArgumentCaptor.forClass(FileEntity.class);
    when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    FileEntity savedEntity = fileStorageService.storeFile(multipartFile);

    assertThat(savedEntity).isNotNull();
    assertThat(savedEntity.getFileName()).isEqualTo("test-aboba.txt");
    assertThat(savedEntity.getContentType()).isEqualTo(MediaType.TEXT_PLAIN_VALUE);
    assertThat(savedEntity.getSize()).isEqualTo(multipartFile.getSize());
    assertThat(savedEntity.getUploadTimestamp()).isNotNull();
    assertThat(savedEntity.getFilePath()).startsWith(tempDir.toString());
    assertTrue(Files.exists(Path.of(savedEntity.getFilePath())));

    verify(fileRepository).save(fileEntityArgumentCaptor.capture());
    FileEntity capturedEntity = fileEntityArgumentCaptor.getValue();
    assertThat(capturedEntity.getFileName()).isEqualTo("test-aboba.txt");
  }

  @Test
  void storeFileErrorTest() throws IOException {
      MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
      when(multipartFile.getOriginalFilename()).thenReturn("error-aboba.txt");
      when(multipartFile.getInputStream()).thenThrow(new IOException("Simulated aboba stream error"));

      IOException exception = assertThrows(IOException.class, () -> {
        fileStorageService.storeFile(multipartFile);
      });
      assertTrue(exception.getMessage().contains("error-aboba.txt"));
  }


  @Test
  void getMetadataOkTest() {
    UUID fileId = UUID.randomUUID();
    FileEntity mockEntity = new FileEntity("aboba.txt", "text/plain", 100L, LocalDateTime.now(), "/path/to/aboba.txt", "hash-aboba");
    mockEntity.setId(fileId);
    when(fileRepository.findById(fileId)).thenReturn(Optional.of(mockEntity));

    Optional<FileEntity> foundEntityOptional = fileStorageService.getFileMetadata(fileId);

    assertTrue(foundEntityOptional.isPresent());
    assertEquals(mockEntity, foundEntityOptional.get());
    verify(fileRepository).findById(fileId);
  }

  @Test
  void getMetadataNotFoundTest() {
    UUID fileId = UUID.randomUUID();
    when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

    Optional<FileEntity> foundEntityOptional = fileStorageService.getFileMetadata(fileId);

    assertTrue(foundEntityOptional.isEmpty());
    verify(fileRepository).findById(fileId);
  }

  @Test
  void loadResourceOkTest() throws IOException {
    UUID fileId = UUID.randomUUID();
    String fileName = "readable-aboba.txt";
    Path filePath = tempDir.resolve(fileName);
    Files.writeString(filePath, "Aboba test content");

    FileEntity mockEntity = new FileEntity(fileName, "text/plain", 12L, LocalDateTime.now(), filePath.toString(), "hash-kek");
    mockEntity.setId(fileId);
    when(fileRepository.findById(fileId)).thenReturn(Optional.of(mockEntity));
    Optional<Resource> resourceOptional = fileStorageService.loadFileAsResource(fileId);

    assertTrue(resourceOptional.isPresent());
    Resource resource = resourceOptional.get();
    assertTrue(resource.exists());
    assertTrue(resource.isReadable());
    assertEquals(fileName, resource.getFilename());
  }

  @Test
  void loadResourceNoEntityTest() {
    UUID fileId = UUID.randomUUID();
    when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
    Optional<Resource> resourceOptional = fileStorageService.loadFileAsResource(fileId);
    assertTrue(resourceOptional.isEmpty());
  }

  @Test
  void loadResourceNoFileOnDiskTest() {
    UUID fileId = UUID.randomUUID();
    FileEntity mockEntity = new FileEntity("ghost-aboba.txt", "text/plain", 0L, LocalDateTime.now(), tempDir.resolve("non-existent-aboba.txt").toString(), "hash-lol");
    mockEntity.setId(fileId);
    when(fileRepository.findById(fileId)).thenReturn(Optional.of(mockEntity));

    Optional<Resource> resourceOptional = fileStorageService.loadFileAsResource(fileId);
    assertTrue(resourceOptional.isEmpty());
  }

}
