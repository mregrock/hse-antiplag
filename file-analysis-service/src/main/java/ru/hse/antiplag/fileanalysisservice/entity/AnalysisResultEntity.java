package ru.hse.antiplag.fileanalysisservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;
import java.time.LocalDateTime;

/**
 * Entity representing the result of a file analysis.
 */
@Entity
@Table(name = "analysis_results")
public class AnalysisResultEntity {

  @Id
  @Column(name = "file_id", nullable = false, unique = true)
  private String fileId;

  @Column(name = "paragraph_count", nullable = false)
  private int paragraphCount;

  @Column(name = "word_count", nullable = false)
  private int wordCount;

  @Column(name = "character_count", nullable = false)
  private int characterCount;

  @Lob
  @Column(name = "word_cloud_path")
  private String wordCloudPath;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Default constructor for JPA.
   */
  public AnalysisResultEntity() {
  }

  /**
   * Constructs a new AnalysisResultEntity.
   *
   * @param fileId the id of the original file
   * @param paragraphCount the number of paragraphs
   * @param wordCount the number of words
   * @param characterCount the number of characters
   * @param wordCloudPath the path to the word cloud image
   */
  public AnalysisResultEntity(String fileId, int paragraphCount, int wordCount, int characterCount, String wordCloudPath) {
    this.fileId = fileId;
    this.paragraphCount = paragraphCount;
    this.wordCount = wordCount;
    this.characterCount = characterCount;
    this.wordCloudPath = wordCloudPath;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Gets the original file ID.
   * @return the file ID.
   */
  public String getFileId() {
    return fileId;
  }

  /**
   * Sets the original file ID.
   * @param fileId the file ID.
   */
  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  /**
   * Gets the paragraph count.
   * @return the paragraph count.
   */
  public int getParagraphCount() {
    return paragraphCount;
  }

  /**
   * Sets the paragraph count.
   * @param paragraphCount the paragraph count.
   */
  public void setParagraphCount(int paragraphCount) {
    this.paragraphCount = paragraphCount;
  }

  /**
   * Gets the word count.
   * @return the word count.
   */
  public int getWordCount() {
    return wordCount;
  }

  /**
   * Sets the word count.
   * @param wordCount the word count.
   */
  public void setWordCount(int wordCount) {
    this.wordCount = wordCount;
  }

  /**
   * Gets the character count.
   * @return the character count.
   */
  public int getCharacterCount() {
    return characterCount;
  }

  /**
   * Sets the character count.
   * @param characterCount the character count.
   */
  public void setCharacterCount(int characterCount) {
    this.characterCount = characterCount;
  }

  /**
   * Gets the path to the word cloud image.
   * @return the path to the word cloud image.
   */
  public String getWordCloudPath() {
    return wordCloudPath;
  }

  /**
   * Sets the path to the word cloud image.
   * @param wordCloudPath the path to the word cloud image.
   */
  public void setWordCloudPath(String wordCloudPath) {
    this.wordCloudPath = wordCloudPath;
  }

  /**
   * Gets the creation timestamp.
   * @return the creation timestamp.
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Sets the creation timestamp.
   * @param createdAt the creation timestamp.
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Gets the update timestamp.
   * @return the update timestamp.
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Sets the update timestamp.
   * @param updatedAt the update timestamp.
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
