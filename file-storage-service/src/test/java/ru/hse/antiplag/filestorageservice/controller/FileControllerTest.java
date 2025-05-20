 package ru.hse.antiplag.filestorageservice.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.antiplag.filestorageservice.domain.FileEntity;
import ru.hse.antiplag.filestorageservice.service.FileStorageService;

@WebMvcTest(FileController.class)
class FileControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FileStorageService fileStorageService;

  @Test
  void uploadFile_shouldReturnFileIdAndName_whenUploadIsSuccessful() throws Exception {
    UUID fileId = UUID.randomUUID();
    String fileName = "test-upload.txt";
    MockMultipartFile multipartFile = new MockMultipartFile(
        "file",
        fileName,
        MediaType.TEXT_PLAIN_VALUE,
        "Test content".getBytes(StandardCharsets.UTF_8)
    );

    FileEntity mockEntity = new FileEntity(fileName, MediaType.TEXT_PLAIN_VALUE, 12L, LocalDateTime.now(), "/path/to/" + fileName, "hash");
    mockEntity.setId(fileId);

    given(fileStorageService.storeFile(any(MockMultipartFile.class))).willReturn(mockEntity);

    mockMvc.perform(multipart("/api/v1/files/upload").file(multipartFile))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(fileId.toString())))
        .andExpect(jsonPath("$.fileName", is(fileName)));
  }

  @Test
  void uploadFile_shouldReturnBadRequest_whenFileIsEmpty() throws Exception {
    MockMultipartFile emptyFile = new MockMultipartFile(
        "file",
        "empty.txt",
        MediaType.TEXT_PLAIN_VALUE,
        new byte[0]
    );

    mockMvc.perform(multipart("/api/v1/files/upload").file(emptyFile))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("File is empty"));
  }

  @Test
  void uploadFile_shouldReturnInternalServerError_whenStorageFails() throws Exception {
    MockMultipartFile multipartFile = new MockMultipartFile(
        "file", "fail-upload.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes()
    );
    given(fileStorageService.storeFile(any(MockMultipartFile.class))).willThrow(new IOException("Disk full"));

    mockMvc.perform(multipart("/api/v1/files/upload").file(multipartFile))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Could not store file fail-upload.txt. Error: Disk full"));
  }

  @Test
  void downloadFile_shouldReturnFile_whenFileExists() throws Exception {
    UUID fileId = UUID.randomUUID();
    String fileName = "download-me.txt";
    String fileContent = "This is the content!";
    Resource resource = new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8));

    FileEntity mockEntity = new FileEntity(fileName, MediaType.TEXT_PLAIN_VALUE, (long) fileContent.length(), LocalDateTime.now(), "/irrelevant/path/", "hash");

    given(fileStorageService.loadFileAsResource(fileId)).willReturn(Optional.of(resource));
    given(fileStorageService.getFileMetadata(fileId)).willReturn(Optional.of(mockEntity));

    mockMvc.perform(get("/api/v1/files/download/{fileId}", fileId))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
        .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
        .andExpect(content().string(fileContent));
  }

  @Test
  void downloadFile_shouldReturnNotFound_whenResourceNotExists() throws Exception {
    UUID fileId = UUID.randomUUID();
    given(fileStorageService.loadFileAsResource(fileId)).willReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/files/download/{fileId}", fileId))
        .andExpect(status().isNotFound());
  }

  @Test
  void getFileMetadata_shouldReturnMetadata_whenFileExists() throws Exception {
    UUID fileId = UUID.randomUUID();
    String fileName = "metadata-test.txt";
    FileEntity mockEntity = new FileEntity(fileName, "application/json", 200L, LocalDateTime.now(), "/path/to/meta.json", "hash");
    mockEntity.setId(fileId);
    given(fileStorageService.getFileMetadata(fileId)).willReturn(Optional.of(mockEntity));

    mockMvc.perform(get("/api/v1/files/{fileId}/metadata", fileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(fileId.toString())))
        .andExpect(jsonPath("$.fileName", is(fileName)))
        .andExpect(jsonPath("$.contentType", is("application/json")))
        .andExpect(jsonPath("$.size", is(200)));
  }

  @Test
  void getFileMetadata_shouldReturnNotFound_whenFileNotExists() throws Exception {
    UUID fileId = UUID.randomUUID();
    given(fileStorageService.getFileMetadata(fileId)).willReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/files/{fileId}/metadata", fileId))
        .andExpect(status().isNotFound());
  }
}
