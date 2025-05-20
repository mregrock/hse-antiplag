package ru.hse.antiplag.fileanalysisservice.dto;

/**
 * DTO for the word cloud generation request to QuickChart API.
 */
public class WordCloudRequest {
  private String text;
  private String format = "png";
  private int width = 800;
  private int height = 600;
  private boolean removeStopwords = true;
  private String language = "ru";

  /**
   * Default constructor for JSON deserialization.
   */
  public WordCloudRequest() {
  }

  /**
   * Constructor for WordCloudRequest.
   *
   * @param text the text to generate word cloud from.
   */
  public WordCloudRequest(String text) {
    this.text = text;
  }

  /**
   * Gets the text for the word cloud.
   * @return the text.
   */
  public String getText() {
    return text;
  }

  /**
   * Sets the text for the word cloud.
   * @param text the text.
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Gets the format of the image.
   * @return the image format.
   */
  public String getFormat() {
    return format;
  }

  /**
   * Sets the format of the image.
   * @param format the image format.
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Gets the width of the image.
   * @return the image width.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets the width of the image.
   * @param width the image width.
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Gets the height of the image.
   * @return the image height.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets the height of the image.
   * @param height the image height.
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Checks if stopwords should be removed.
   * @return true if stopwords should be removed, false otherwise.
   */
  public boolean isRemoveStopwords() {
    return removeStopwords;
  }

  /**
   * Sets whether stopwords should be removed.
   * @param removeStopwords true to remove stopwords, false otherwise.
   */
  public void setRemoveStopwords(boolean removeStopwords) {
    this.removeStopwords = removeStopwords;
  }

  /**
   * Gets the language for stopwords.
   * @return the language code.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets the language for stopwords.
   * @param language the language code.
   */
  public void setLanguage(String language) {
    this.language = language;
  }
}
