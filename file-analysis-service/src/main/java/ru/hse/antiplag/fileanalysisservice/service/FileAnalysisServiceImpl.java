package ru.hse.antiplag.fileanalysisservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ru.hse.antiplag.fileanalysisservice.dto.AnalysisResult;
import ru.hse.antiplag.fileanalysisservice.dto.TextStatistics;
import ru.hse.antiplag.fileanalysisservice.dto.WordCloudRequest;
import ru.hse.antiplag.fileanalysisservice.entity.AnalysisResultEntity;
import ru.hse.antiplag.fileanalysisservice.repository.AnalysisResultRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link FileAnalysisService} interface.
 */
@Service
public class FileAnalysisServiceImpl implements FileAnalysisService {

  private static final Logger logger = LoggerFactory.getLogger(FileAnalysisServiceImpl.class);

  private final WebClient fileStorageWebClient;
  private final WebClient wordCloudWebClient;
  private final AnalysisResultRepository analysisResultRepository;

  private static class FileStorageUploadResponse {
    private UUID id;
    private String fileName;

    public UUID getId() {
      return id;
    }

    public void setId(UUID id) {
      this.id = id;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }
  }

  /**
   * Constructs a {@code FileAnalysisServiceImpl}.
   *
   * @param fileStorageWebClient the web client for file storage service.
   * @param wordCloudWebClient the web client for word cloud service.
   */
  @Autowired
  public FileAnalysisServiceImpl(
      @Qualifier("fileStorageWebClient") WebClient fileStorageWebClient,
      @Qualifier("wordCloudWebClient") WebClient wordCloudWebClient,
      AnalysisResultRepository analysisResultRepository) {
    this.fileStorageWebClient = fileStorageWebClient;
    this.wordCloudWebClient = wordCloudWebClient;
    this.analysisResultRepository = analysisResultRepository;
  }

  /**
   * Analyzes the file with the given ID.
   *
   * @param fileId the ID of the file to analyze.
   * @return the result of the analysis.
   */
  @Override
  @Transactional
  public AnalysisResult analyzeFile(String fileId) {
    logger.info("Attempting to analyze file with ID: {}", fileId);

    Optional<AnalysisResultEntity> existingResultOpt = analysisResultRepository.findByFileId(fileId);
    if (existingResultOpt.isPresent()) {
      AnalysisResultEntity existingEntity = existingResultOpt.get();
      logger.info("Found existing analysis result for fileId: {}. Returning cached data.", fileId);
      existingEntity.setUpdatedAt(LocalDateTime.now());
      analysisResultRepository.save(existingEntity);
      return convertToDto(existingEntity);
    }

    logger.info("No cached result for fileId: {}. Proceeding with full analysis.", fileId);
    String fileContent = fetchFileContent(fileId);

    if (fileContent == null) {
      logger.warn("File content is null for fileId: {}. Cannot perform analysis.", fileId);
      return new AnalysisResult(new TextStatistics(0, 0, 0), "");
    }
    if (fileContent.trim().isEmpty()){
        logger.info("File content is empty for fileId: {}. Caching empty stats.", fileId);
        AnalysisResultEntity emptyEntity = new AnalysisResultEntity(fileId, 0,0,0, "");
        analysisResultRepository.save(emptyEntity);
        return convertToDto(emptyEntity);
    }

    int paragraphCount = calculateParagraphCount(fileContent);
    int wordCount = calculateWordCount(fileContent);
    int characterCount = fileContent.length();

    TextStatistics stats = new TextStatistics(paragraphCount, wordCount, characterCount);
    logger.info("Calculated statistics for fileId: {}: {}", fileId, stats);

    String wordCloudFileId = generateAndStoreWordCloud(fileId, fileContent);
    String wordCloudPath = "";
    if (wordCloudFileId != null && !wordCloudFileId.isEmpty()) {
      wordCloudPath = "/api/gateway/analysis/wordcloud/" + wordCloudFileId;
    } else if (wordCloudFileId == null && (fileContent != null && !fileContent.trim().isEmpty())) {
      logger.warn("Word cloud generation/storage failed for non-empty fileId: {}. Returning stats without saving.", fileId);
      TextStatistics currentStats = new TextStatistics(paragraphCount, wordCount, characterCount);
      return new AnalysisResult(currentStats, "");
    }

    AnalysisResultEntity newEntity = new AnalysisResultEntity(fileId, paragraphCount, wordCount, characterCount, wordCloudPath);
    analysisResultRepository.save(newEntity);
    logger.info("Saved new analysis result for fileId: {}", fileId);

    return convertToDto(newEntity);
  }

  private String fetchFileContent(String fileId) {
    try {
      logger.debug("Fetching file content for fileId: {}", fileId);
      Resource resource = fileStorageWebClient.get()
          .uri("/download/{fileId}", fileId)
          .retrieve()
          .bodyToMono(Resource.class)
          .block();

      if (resource == null || !resource.exists() || !resource.isReadable()) {
        logger.warn("Resource not found or not readable for fileId: {}", fileId);
        return null;
      }

      try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
        return FileCopyUtils.copyToString(reader);
      } catch (IOException e) {
        logger.error("IOException while reading resource for fileId: {}: {}", fileId, e.getMessage(), e);
        return null;
      }

    } catch (Exception e) {
      logger.error("Error fetching file content for fileId: {}: {}", fileId, e.getMessage());
      return null;
    }
  }

  private String generateAndStoreWordCloud(String originalFileId, String textContent) {
    if (textContent == null || textContent.trim().isEmpty()) {
      logger.info("Text content is empty for fileId: {}. Skipping word cloud generation.", originalFileId);
      return null;
    }
    try {
      WordCloudRequest request = new WordCloudRequest(textContent);
      request.setFormat("png");

      logger.info("Requesting word cloud for originalFileId: {}", originalFileId);
      byte[] imageBytes = wordCloudWebClient.post()
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(request))
          .retrieve()
          .bodyToMono(byte[].class)
          .block();

      if (imageBytes == null || imageBytes.length == 0) {
        logger.warn("Received empty image bytes from word cloud API for originalFileId: {}", originalFileId);
        return null;
      }
      logger.info("Successfully received word cloud image for originalFileId: {}. Size: {} bytes", originalFileId, imageBytes.length);

      MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
      bodyBuilder.part("file", new ByteArrayResource(imageBytes))
          .filename("wordcloud_" + originalFileId + ".png");

      logger.info("Uploading word cloud image to FileStorageService for originalFileId: {}", originalFileId);
      FileStorageUploadResponse fsResponse = fileStorageWebClient.post()
          .uri("/upload")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
          .retrieve()
          .bodyToMono(FileStorageUploadResponse.class)
          .block();

      if (fsResponse != null && fsResponse.getId() != null) {
        logger.info("Successfully uploaded word cloud image. Stored file ID: {}", fsResponse.getId());
        return fsResponse.getId().toString();
      } else {
        logger.warn("Failed to upload word cloud image or received no ID from FileStorageService for originalFileId: {}", originalFileId);
        return null;
      }

    } catch (Exception e) {
      logger.error("Error generating or storing word cloud for originalFileId: {}: {}", originalFileId, e.getMessage(), e);
      return null;
    }
  }

  private int calculateParagraphCount(String content) {
    if (content == null || content.trim().isEmpty()) {
      return 0;
    }
    String normalizedContent = content.replace("\r\n", "\n").replace("\r", "\n");
    String effectivelyTrimmedContent = normalizedContent.trim();

    if (effectivelyTrimmedContent.isEmpty()) {
        return 0;
    }

    String[] paragraphs = effectivelyTrimmedContent.split("(\n\s*){2,}");

    int actualParagraphs = 0;
    for (String p : paragraphs) {
        if (!p.trim().isEmpty()) {
            actualParagraphs++;
        }
    }
    return actualParagraphs > 0 ? actualParagraphs : 1;
  }

  private int calculateWordCount(String content) {
    if (content == null) return 0;
    String trimmedContent = content.trim();
    if (trimmedContent.isEmpty()) return 0;
    String[] words = trimmedContent.split("\\s+");
    return words.length;
  }

  private AnalysisResult convertToDto(AnalysisResultEntity entity) {
    TextStatistics stats = new TextStatistics(
        entity.getParagraphCount(),
        entity.getWordCount(),
        entity.getCharacterCount()
    );
    return new AnalysisResult(stats, entity.getWordCloudPath());
  }

  @Override
  public Resource getWordCloudResource(String wordCloudImageId) {
    logger.info("Attempting to fetch word cloud resource with ID: {}", wordCloudImageId);
    try {
      Resource resource = fileStorageWebClient.get()
          .uri("/download/{fileId}", wordCloudImageId)
          .accept(MediaType.IMAGE_PNG)
          .retrieve()
          .bodyToMono(Resource.class)
          .block();

      if (resource == null || !resource.exists() || !resource.isReadable()) {
        logger.warn("Word cloud resource not found or not readable for ID: {}", wordCloudImageId);
        return null;
      }
      logger.info("Successfully fetched word cloud resource for ID: {}", wordCloudImageId);
      return resource;
    } catch (Exception e) {
      logger.error("Error fetching word cloud resource for ID: {}: {}", wordCloudImageId, e.getMessage(), e);
      return null;
    }
  }
}
