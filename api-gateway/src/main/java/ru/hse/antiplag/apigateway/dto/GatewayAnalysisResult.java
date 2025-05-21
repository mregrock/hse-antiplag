package ru.hse.antiplag.apigateway.dto;

import java.util.Objects;

/**
 * Data Transfer Object for the complete analysis result.
 */
public class GatewayAnalysisResult {

  private GatewayTextStatistics textStatistics;
  private String wordCloudPath;

  /**
   * Default constructor for GatewayAnalysisResult.
   */
  public GatewayAnalysisResult() {
  }

  /**
   * Constructs a new GatewayAnalysisResult.
   *
   * @param textStatistics the text statistics.
   * @param wordCloudPath  the path to the word cloud image.
   */
  public GatewayAnalysisResult(GatewayTextStatistics textStatistics, String wordCloudPath) {
    this.textStatistics = textStatistics;
    this.wordCloudPath = wordCloudPath;
  }

  /**
   * Gets the text statistics.
   *
   * @return the text statistics.
   */
  public GatewayTextStatistics getTextStatistics() {
    return textStatistics;
  }

  /**
   * Sets the text statistics.
   *
   * @param textStatistics the new text statistics.
   */
  public void setTextStatistics(GatewayTextStatistics textStatistics) {
    this.textStatistics = textStatistics;
  }

  /**
   * Gets the word cloud path.
   *
   * @return the word cloud path.
   */
  public String getWordCloudPath() {
    return wordCloudPath;
  }

  /**
   * Sets the word cloud path.
   *
   * @param wordCloudPath the new word cloud path.
   */
  public void setWordCloudPath(String wordCloudPath) {
    this.wordCloudPath = wordCloudPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayAnalysisResult that = (GatewayAnalysisResult) o;
    return Objects.equals(textStatistics, that.textStatistics) &&
           Objects.equals(wordCloudPath, that.wordCloudPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(textStatistics, wordCloudPath);
  }

  @Override
  public String toString() {
    return "GatewayAnalysisResult{" +
           "textStatistics=" + textStatistics +
           ", wordCloudPath='" + wordCloudPath + '\'' +
           '}';
  }
}
