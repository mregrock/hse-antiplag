package ru.hse.antiplag.filestorageservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.hse.antiplag.filestorageservice.domain.FileEntity;
import ru.hse.antiplag.filestorageservice.repository.FileRepository;
import ru.hse.antiplag.filestorageservice.utils.FileHashUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the service for managing file storage.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

  private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

  private final Path fileStorageLocation;
  private final FileRepository fileRepository;

  /**
   * Constructor for FileStorageServiceImpl.
   *
   * @param uploadDir      path to the directory for uploading files (from application.properties)
   * @param fileRepository repository for working with file metadata
   * @throws IOException if the directory for storing files could not be created
   */
  @Autowired
  public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir, FileRepository fileRepository) throws IOException {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    this.fileRepository = fileRepository;
    Files.createDirectories(this.fileStorageLocation);
    logger.info("File storage location initialized at: {}", this.fileStorageLocation);
  }

  @Override
  @Transactional
  public FileEntity storeFile(MultipartFile file) throws IOException {
    String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

    String hash;
    try (InputStream inputStream = file.getInputStream()) {
      hash = FileHashUtil.calculateSHA256(inputStream);
    } catch (Exception e) {
      logger.error("Error calculating hash for file {}. Error: {}", originalFileName, e.getMessage());
      throw new IOException("Error calculating hash for file " + originalFileName + ". Please try again!", e);
    }

    Optional<FileEntity> existingFile = fileRepository.findByHash(hash);
    if (existingFile.isPresent()) {
      logger.info("File with hash {} already exists. Returning existing file.", hash);
      return existingFile.get();
    }

    String fileExtension = "";
    int i = originalFileName.lastIndexOf('.');
    if (i > 0) {
      fileExtension = originalFileName.substring(i);
    }
    String storedFileName = UUID.randomUUID().toString() + fileExtension;
    Path targetLocation = this.fileStorageLocation.resolve(storedFileName);

    try {
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
      logger.info("Stored file {} at location {}", originalFileName, targetLocation);
    } catch (IOException ex) {
      logger.error("Could not store file {}. Error: {}", originalFileName, ex.getMessage());
      throw new IOException("Could not store file " + originalFileName + ". Please try again!", ex);
    }

    FileEntity fileEntity = new FileEntity(
        originalFileName,
        file.getContentType(),
        file.getSize(),
        LocalDateTime.now(),
        targetLocation.toString(),
        hash
    );
    return fileRepository.save(fileEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<FileEntity> getFileMetadata(UUID fileId) {
    return fileRepository.findById(fileId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Resource> loadFileAsResource(UUID fileId) {
    Optional<FileEntity> fileEntityOptional = fileRepository.findById(fileId);
    if (fileEntityOptional.isEmpty()) {
      return Optional.empty();
    }

    try {
      Path filePath = Paths.get(fileEntityOptional.get().getFilePath()).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists() && resource.isReadable()) {
        return Optional.of(resource);
      } else {
        logger.warn("Could not read file: {}", filePath);
        return Optional.empty();
      }
    } catch (MalformedURLException ex) {
      logger.error("Error creating URL for file path: {}. Error: {}", fileEntityOptional.get().getFilePath(), ex.getMessage());
      return Optional.empty();
    }
  }
}
