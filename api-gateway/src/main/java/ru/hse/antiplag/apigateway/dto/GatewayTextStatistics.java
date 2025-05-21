package ru.hse.antiplag.apigateway.dto;

import java.util.Objects;

/**
 * Data Transfer Object for text statistics.
 */
public class GatewayTextStatistics {

  private int paragraphCount;
  private int wordCount;
  private int characterCount;

  /**
   * Default constructor for GatewayTextStatistics.
   * Required for deserialization.
   */
  public GatewayTextStatistics() {
  }

  /**
   * Constructs a new GatewayTextStatistics with specified counts.
   *
   * @param paragraphCount the number of paragraphs.
   * @param wordCount      the number of words.
   * @param characterCount the number of characters.
   */
  public GatewayTextStatistics(int paragraphCount, int wordCount, int characterCount) {
    this.paragraphCount = paragraphCount;
    this.wordCount = wordCount;
    this.characterCount = characterCount;
  }

  /**
   * Gets the paragraph count.
   *
   * @return the paragraph count.
   */
  public int getParagraphCount() {
    return paragraphCount;
  }

  /**
   * Sets the paragraph count.
   *
   * @param paragraphCount the new paragraph count.
   */
  public void setParagraphCount(int paragraphCount) {
    this.paragraphCount = paragraphCount;
  }

  /**
   * Gets the word count.
   *
   * @return the word count.
   */
  public int getWordCount() {
    return wordCount;
  }

  /**
   * Sets the word count.
   *
   * @param wordCount the new word count.
   */
  public void setWordCount(int wordCount) {
    this.wordCount = wordCount;
  }

  /**
   * Gets the character count.
   *
   * @return the character count.
   */
  public int getCharacterCount() {
    return characterCount;
  }

  /**
   * Sets the character count.
   *
   * @param characterCount the new character count.
   */
  public void setCharacterCount(int characterCount) {
    this.characterCount = characterCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayTextStatistics that = (GatewayTextStatistics) o;
    return paragraphCount == that.paragraphCount &&
           wordCount == that.wordCount &&
           characterCount == that.characterCount;
  }

  @Override
  public int hashCode() {
    return Objects.hash(paragraphCount, wordCount, characterCount);
  }

  @Override
  public String toString() {
    return "GatewayTextStatistics{" +
           "paragraphCount=" + paragraphCount +
           ", wordCount=" + wordCount +
           ", characterCount=" + characterCount +
           '}';
  }
}
