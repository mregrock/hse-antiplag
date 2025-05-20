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
  void storeFile_shouldSaveFileAndMetadata() throws IOException {
    MockMultipartFile multipartFile = new MockMultipartFile(
        "file",
        "test-file.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "Hello, World!".getBytes()
    );

    ArgumentCaptor<FileEntity> fileEntityArgumentCaptor = ArgumentCaptor.forClass(FileEntity.class);
    when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    FileEntity savedEntity = fileStorageService.storeFile(multipartFile);

    assertThat(savedEntity).isNotNull();
    assertThat(savedEntity.getFileName()).isEqualTo("test-file.txt");
    assertThat(savedEntity.getContentType()).isEqualTo(MediaType.TEXT_PLAIN_VALUE);
    assertThat(savedEntity.getSize()).isEqualTo(multipartFile.getSize());
    assertThat(savedEntity.getUploadTimestamp()).isNotNull();
    assertThat(savedEntity.getFilePath()).startsWith(tempDir.toString());
    assertTrue(Files.exists(Path.of(savedEntity.getFilePath())));

    verify(fileRepository).save(fileEntityArgumentCaptor.capture());
    FileEntity capturedEntity = fileEntityArgumentCaptor.getValue();
    assertThat(capturedEntity.getFileName()).isEqualTo("test-file.txt");
  }

  @Test
  void storeFile_shouldThrowIOExceptionWhenCopyFails() throws IOException {
      MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
      when(multipartFile.getOriginalFilename()).thenReturn("error-file.txt");
      when(multipartFile.getInputStream()).thenThrow(new IOException("Simulated stream error"));

      IOException exception = assertThrows(IOException.class, () -> {
        fileStorageService.storeFile(multipartFile);
      });
      assertTrue(exception.getMessage().contains("Could not store file error-file.txt"));
  }


  @Test
  void getFileMetadata_shouldReturnFileEntity_whenFileExists() {
    UUID fileId = UUID.randomUUID();
    FileEntity mockEntity = new FileEntity("test.txt", "text/plain", 100L, LocalDateTime.now(), "/path/to/file.txt");
    mockEntity.setId(fileId);
    when(fileRepository.findById(fileId)).thenReturn(Optional.of(mockEntity));

    Optional<FileEntity> foundEntityOptional = fileStorageService.getFileMetadata(fileId);

    assertTrue(foundEntityOptional.isPresent());
    assertEquals(mockEntity, foundEntityOptional.get());
    verify(fileRepository).findById(fileId);
  }

  @Test
  void getFileMetadata_shouldReturnEmptyOptional_whenFileDoesNotExist() {
    UUID fileId = UUID.randomUUID();
    when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

    Optional<FileEntity> foundEntityOptional = fileStorageService.getFileMetadata(fileId);

    assertTrue(foundEntityOptional.isEmpty());
    verify(fileRepository).findById(fileId);
  }

  @Test
  void loadFileAsResource_shouldReturnResource_whenFileExistsAndReadable() throws IOException {
    UUID fileId = UUID.randomUUID();
    String fileName = "readable-test.txt";
    Path filePath = tempDir.resolve(fileName);
    Files.writeString(filePath, "Test content");

    FileEntity mockEntity = new FileEntity(fileName, "text/plain", 12L, LocalDateTime.now(), filePath.toString());
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
  void loadFileAsResource_shouldReturnEmpty_whenFileEntityNotFound() {
    UUID fileId = UUID.randomUUID();
    when(fileRepository.findById(fileId)).thenReturn(Optional.empty());
    Optional<Resource> resourceOptional = fileStorageService.loadFileAsResource(fileId);
    assertTrue(resourceOptional.isEmpty());
  }

  @Test
  void loadFileAsResource_shouldReturnEmpty_whenFileNotExistsOnDisk() {
    UUID fileId = UUID.randomUUID();
    FileEntity mockEntity = new FileEntity("ghost-file.txt", "text/plain", 0L, LocalDateTime.now(), tempDir.resolve("non-existent-file.txt").toString());
    mockEntity.setId(fileId);
    when(fileRepository.findById(fileId)).thenReturn(Optional.of(mockEntity));

    Optional<Resource> resourceOptional = fileStorageService.loadFileAsResource(fileId);
    assertTrue(resourceOptional.isEmpty());
  }

}
