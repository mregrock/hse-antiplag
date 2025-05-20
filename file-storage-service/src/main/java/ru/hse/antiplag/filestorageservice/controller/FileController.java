package ru.hse.antiplag.filestorageservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.hse.antiplag.filestorageservice.domain.FileEntity;
import ru.hse.antiplag.filestorageservice.service.FileStorageService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing files.
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

  private static final Logger logger = LoggerFactory.getLogger(FileController.class);

  private final FileStorageService fileStorageService;

  @Autowired
  public FileController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  /**
   * Endpoint for uploading a file.
   *
   * @param file uploaded file
   * @return ResponseEntity with metadata of the saved file or an error
   */
  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("File is empty");
    }
    try {
      FileEntity storedFile = fileStorageService.storeFile(file);
      UploadFileResponse response = new UploadFileResponse(storedFile.getId(), storedFile.getFileName());
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IOException e) {
      logger.error("Could not store file: {}", file.getOriginalFilename(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Could not store file " + file.getOriginalFilename() + ". Error: " + e.getMessage());
    }
  }

  /**
   * Endpoint for downloading a file by its ID.
   *
   * @param fileId ID of the file
   * @return ResponseEntity with the file or a 404 error
   */
  @GetMapping("/download/{fileId}")
  public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) {
    Optional<Resource> resourceOptional = fileStorageService.loadFileAsResource(fileId);
    if (resourceOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Resource resource = resourceOptional.get();
    Optional<FileEntity> fileEntityOptional = fileStorageService.getFileMetadata(fileId);

    String contentType = "application/octet-stream";
    String originalFileName = "downloaded-file";

    if (fileEntityOptional.isPresent()) {
      contentType = fileEntityOptional.get().getContentType();
      originalFileName = fileEntityOptional.get().getFileName();
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
        .body(resource);
  }

  /**
   * Endpoint for getting the metadata of a file by its ID.
   *
   * @param fileId ID of the file
   * @return ResponseEntity with FileEntity or a 404
   */
  @GetMapping("/{fileId}/metadata")
  public ResponseEntity<FileEntity> getFileMetadata(@PathVariable UUID fileId) {
    return fileStorageService.getFileMetadata(fileId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Inner class for the response when a file is uploaded.
   */
  private static class UploadFileResponse {
    private UUID id;
    private String fileName;

    public UploadFileResponse(UUID id, String fileName) {
      this.id = id;
      this.fileName = fileName;
    }

    public UUID getId() {
      return id;
    }

    public String getFileName() {
      return fileName;
    }
  }
}
