package ru.hse.antiplag.fileanalysisservice.dto;

/**
 * DTO for text statistics.
 */
public class TextStatistics {
  private int paragraphCount;
  private int wordCount;
  private int characterCount;

  /**
   * Default constructor.
   */
  public TextStatistics() {
  }

  /**
   * Constructor with all fields.
   *
   * @param paragraphCount number of paragraphs.
   * @param wordCount number of words.
   * @param characterCount number of characters.
   */
  public TextStatistics(int paragraphCount, int wordCount, int characterCount) {
    this.paragraphCount = paragraphCount;
    this.wordCount = wordCount;
    this.characterCount = characterCount;
  }

  /**
   * Gets the number of paragraphs.
   * @return number of paragraphs.
   */
  public int getParagraphCount() {
    return paragraphCount;
  }

  /**
   * Sets the number of paragraphs.
   * @param paragraphCount number of paragraphs.
   */
  public void setParagraphCount(int paragraphCount) {
    this.paragraphCount = paragraphCount;
  }

  /**
   * Gets the number of words.
   * @return number of words.
   */
  public int getWordCount() {
    return wordCount;
  }

  /**
   * Sets the number of words.
   * @param wordCount number of words.
   */
  public void setWordCount(int wordCount) {
    this.wordCount = wordCount;
  }

  /**
   * Gets the number of characters.
   * @return number of characters.
   */
  public int getCharacterCount() {
    return characterCount;
  }

  /**
   * Sets the number of characters.
   * @param characterCount number of characters.
   */
  public void setCharacterCount(int characterCount) {
    this.characterCount = characterCount;
  }
}
