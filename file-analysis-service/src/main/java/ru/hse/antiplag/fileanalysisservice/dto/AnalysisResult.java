package ru.hse.antiplag.fileanalysisservice.dto;

/**
 * DTO for the complete analysis result.
 */
public class AnalysisResult {
  private TextStatistics textStatistics;
  private String wordCloudPath;

  /**
   * Default constructor.
   */
  public AnalysisResult() {
  }

  /**
   * Constructor with all fields.
   *
   * @param textStatistics the calculated text statistics.
   * @param wordCloudPath the path to the saved word cloud image.
   */
  public AnalysisResult(TextStatistics textStatistics, String wordCloudPath) {
    this.textStatistics = textStatistics;
    this.wordCloudPath = wordCloudPath;
  }

  /**
   * Gets the text statistics.
   * @return the text statistics.
   */
  public TextStatistics getTextStatistics() {
    return textStatistics;
  }

  /**
   * Sets the text statistics.
   * @param textStatistics the text statistics.
   */
  public void setTextStatistics(TextStatistics textStatistics) {
    this.textStatistics = textStatistics;
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
}
