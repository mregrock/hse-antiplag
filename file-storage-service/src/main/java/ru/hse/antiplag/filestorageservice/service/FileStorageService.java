package ru.hse.antiplag.filestorageservice.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.hse.antiplag.filestorageservice.domain.FileEntity;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing file storage.
 */
public interface FileStorageService {

  /**
   * Saves the uploaded file and its metadata.
   *
   * @param file uploaded file
   * @return saved FileEntity entity
   * @throws IOException if an error occurs while saving the file to disk
   */
  FileEntity storeFile(MultipartFile file) throws IOException;

  /**
   * Finds the metadata of a file by its ID.
   *
   * @param fileId ID of the file
   * @return Optional with FileEntity if found, otherwise Optional.empty()
   */
  Optional<FileEntity> getFileMetadata(UUID fileId);

  /**
   * Loads the file as a resource by its ID.
   *
   * @param fileId ID of the file
   * @return Optional with Resource if found and accessible, otherwise Optional.empty()
   */
  Optional<Resource> loadFileAsResource(UUID fileId);
}
