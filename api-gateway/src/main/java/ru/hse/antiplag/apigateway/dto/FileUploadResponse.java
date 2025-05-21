package ru.hse.antiplag.apigateway.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object for the response received after uploading a file via the API Gateway.
 */
public class FileUploadResponse {

  private UUID id;
  private String fileName;

  /**
   * Default constructor for FileUploadResponse.
   */
  public FileUploadResponse() {
  }

  /**
   * Constructs a new FileUploadResponse.
   *
   * @param id       the unique identifier of the uploaded file.
   * @param fileName the name of the uploaded file.
   */
  public FileUploadResponse(UUID id, String fileName) {
    this.id = id;
    this.fileName = fileName;
  }

  /**
   * Gets the ID of the uploaded file.
   *
   * @return the file ID.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets the ID of the uploaded file.
   *
   * @param id the new file ID.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * Gets the name of the uploaded file.
   *
   * @return the file name.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the name of the uploaded file.
   *
   * @param fileName the new file name.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileUploadResponse that = (FileUploadResponse) o;
    return Objects.equals(id, that.id) &&
           Objects.equals(fileName, that.fileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, fileName);
  }

  @Override
  public String toString() {
    return "FileUploadResponse{" +
           "id=" + id +
           ", fileName='" + fileName + '\'' +
           '}';
  }
}
