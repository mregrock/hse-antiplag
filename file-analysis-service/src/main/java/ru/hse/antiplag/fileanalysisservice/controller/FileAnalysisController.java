package ru.hse.antiplag.fileanalysisservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.antiplag.fileanalysisservice.dto.AnalysisResult;
import ru.hse.antiplag.fileanalysisservice.service.FileAnalysisService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/**
 * Controller for handling file analysis requests.
 */
@RestController
@RequestMapping("/api/v1/analysis")
public class FileAnalysisController {

  private final FileAnalysisService fileAnalysisService;

  /**
   * Constructs a {@code FileAnalysisController}.
   *
   * @param fileAnalysisService the service to use for file analysis.
   */
  @Autowired
  public FileAnalysisController(FileAnalysisService fileAnalysisService) {
    this.fileAnalysisService = fileAnalysisService;
  }

  /**
   * Handles the request to analyze a file.
   *
   * @param fileId the ID of the file to analyze.
   * @return a {@link ResponseEntity} containing the {@link AnalysisResult}.
   */
  @GetMapping("/{fileId}")
  public ResponseEntity<AnalysisResult> analyzeFile(@PathVariable String fileId) {
    AnalysisResult result = fileAnalysisService.analyzeFile(fileId);
    return ResponseEntity.ok(result);
  }

  /**
   * Handles the request to retrieve a word cloud image.
   *
   * @param wordCloudImageId the ID of the word cloud image.
   * @return a {@link ResponseEntity} containing the {@link Resource} of the image.
   */
  @GetMapping("/wordcloud/{wordCloudImageId}")
  public ResponseEntity<Resource> getWordCloud(@PathVariable String wordCloudImageId) {
    Resource imageResource = fileAnalysisService.getWordCloudResource(wordCloudImageId);
    if (imageResource == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageResource);
  }
}
