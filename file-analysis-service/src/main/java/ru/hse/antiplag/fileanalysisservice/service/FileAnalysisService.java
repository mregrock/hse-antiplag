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
}

