package ru.hse.antiplag.fileanalysisservice.service;

import ru.hse.antiplag.fileanalysisservice.dto.AnalysisResult;

/**
 * Service interface for file analysis operations.
 */
public interface FileAnalysisService {

  /**
   * Analyzes the file with the given ID.
   *
   * @param fileId the ID of the file to analyze.
   * @return the result of the analysis.
   */
  AnalysisResult analyzeFile(String fileId);

  /**
   * Retrieves the word cloud image as a resource.
   *
   * @param wordCloudImageId the ID of the word cloud image.
   * @return the word cloud image as a Resource.
   */
  org.springframework.core.io.Resource getWordCloudResource(String wordCloudImageId);
}

