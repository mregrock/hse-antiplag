package ru.hse.antiplag.filestorageservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity that represents file metadata.
 */
@Entity
@Table(name = "files")
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false)
  private LocalDateTime uploadTimestamp;

  @Column(nullable = false)
  private String filePath;

  @Column(nullable = false, unique = true)
  private String hash;

  /**
   * Default constructor for JPA.
   */
  public FileEntity() {
  }

  /**
   * Creates a new instance of FileEntity.
   *
   * @param fileName        file name
   * @param contentType     MIME-type of the file
   * @param size            size of the file
   * @param uploadTimestamp timestamp of the upload
   * @param filePath        path to the file
   * @param hash            hash of the file
   */
  public FileEntity(
      String fileName,
      String contentType,
      Long size,
      LocalDateTime uploadTimestamp,
      String filePath,
      String hash) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
    this.uploadTimestamp = uploadTimestamp;
    this.filePath = filePath;
    this.hash = hash;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public LocalDateTime getUploadTimestamp() {
    return uploadTimestamp;
  }

  public void setUploadTimestamp(LocalDateTime uploadTimestamp) {
    this.uploadTimestamp = uploadTimestamp;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public String toString() {
    return "FileEntity{" +
        "id=" + id +
        ", fileName='" + fileName + '\'' +
        ", contentType='" + contentType + '\'' +
        ", size=" + size +
        ", uploadTimestamp=" + uploadTimestamp +
        ", filePath='" + filePath + '\'' +
        '}';
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

}
