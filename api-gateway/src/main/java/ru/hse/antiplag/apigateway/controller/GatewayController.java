package ru.hse.antiplag.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.hse.antiplag.apigateway.dto.FileUploadResponse;
import ru.hse.antiplag.apigateway.dto.GatewayAnalysisResult;
import org.springframework.util.LinkedMultiValueMap;

/**
 * REST controller for handling requests to the API Gateway.
 * It forwards requests to the appropriate downstream services.
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

  private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

  private final WebClient fileAnalysisServiceWebClient;
  private final WebClient fileStorageServiceWebClient;

  /**
   * Constructs a GatewayController with the necessary WebClients.
   *
   * @param fileAnalysisServiceWebClient WebClient configured for FileAnalysisService.
   * @param fileStorageServiceWebClient  WebClient configured for FileStorageService.
   */
  @Autowired
  public GatewayController(WebClient fileAnalysisServiceWebClient, WebClient fileStorageServiceWebClient) {
    this.fileAnalysisServiceWebClient = fileAnalysisServiceWebClient;
    this.fileStorageServiceWebClient = fileStorageServiceWebClient;
  }

  /**
   * Handles requests to analyze a file by its ID.
   *
   * @param fileId the ID of the file to be analyzed.
   * @return A Mono emitting the GatewayAnalysisResult.
   */
  @GetMapping("/analyze/{fileId}")
  public Mono<GatewayAnalysisResult> analyzeFile(@PathVariable String fileId) {
    logger.info("API Gateway received request to analyze fileId: {}", fileId);
    return fileAnalysisServiceWebClient.get()
        .uri("/api/v1/analysis/{fileId}", fileId)
        .retrieve()
        .bodyToMono(GatewayAnalysisResult.class)
        .doOnSuccess(result -> logger.info("Successfully retrieved analysis for fileId: {}. Result: {}", fileId, result))
        .doOnError(error -> logger.error("Error during analysis call for fileId: {}. Error: {}", fileId, error.getMessage()));
  }

  /**
   * Uploads a file by proxying the multipart request to the FileStorageService.
   *
   * @param filePartMono a Mono containing the FilePart for the 'file' part of the multipart request.
   * @return a Mono with FileUploadResponse from FileStorageService.
   */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<FileUploadResponse> uploadFile(
      @RequestPart("file") Mono<FilePart> filePartMono) {
    logger.info("API Gateway received request to upload a file (via filePartMono for 'file' part).");

    return filePartMono.flatMap(filePart -> {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part(filePart.name(), filePart);

        logger.info("Processing part: name='{}', filename='{}', headers='{}'",
            filePart.name(), filePart.filename(), filePart.headers());

        return fileStorageServiceWebClient.post()
            .uri("/api/v1/files/upload")
            .body(BodyInserters.fromMultipartData(builder.build()))
            .retrieve()
            .bodyToMono(FileUploadResponse.class)
            .doOnSuccess(response -> logger.info("Successfully uploaded file via Gateway. Response: {}", response))
            .doOnError(e -> logger.error("Error during file upload via Gateway. Error: {}", e.getMessage(), e));
    });
  }

  /**
   * Handles requests to download a file by its ID.
   *
   * @param fileId the ID of the file to be downloaded.
   * @return A Mono emitting the Resource representing the file.
   */
  @GetMapping("/download/{fileId}")
  public Mono<Resource> downloadFile(@PathVariable String fileId) {
    logger.info("API Gateway received request to download fileId: {}", fileId);
    return fileStorageServiceWebClient.get()
        .uri("/api/v1/files/download/{fileId}", fileId)
        .accept(MediaType.APPLICATION_OCTET_STREAM)
        .retrieve()
        .bodyToMono(Resource.class)
        .doOnSuccess(resource -> logger.info("Successfully retrieved fileId: {}. Resource: {}", fileId, resource.getFilename()))
        .doOnError(error -> logger.error("Error downloading fileId {}: {}", fileId, error.getMessage()));
  }

  /**
   * Handles requests to retrieve a word cloud image by its ID, proxied through FileAnalysisService.
   *
   * @param wordCloudImageId the ID of the word cloud image.
   * @return A Mono emitting the Resource representing the word cloud image.
   */
  @GetMapping("/analysis/wordcloud/{wordCloudImageId}")
  public Mono<Resource> getWordCloudImage(@PathVariable String wordCloudImageId) {
    logger.info("API Gateway received request for word cloud imageId: {}", wordCloudImageId);
    return fileAnalysisServiceWebClient.get()
        .uri("/api/v1/analysis/wordcloud/{wordCloudImageId}", wordCloudImageId)
        .accept(MediaType.IMAGE_PNG)
        .retrieve()
        .bodyToMono(Resource.class)
        .doOnSuccess(resource -> logger.info("Successfully retrieved word cloud imageId: {}. Resource: {}", wordCloudImageId, resource.getFilename()))
        .doOnError(error -> logger.error("Error fetching word cloud imageId {}: {}", wordCloudImageId, error.getMessage()));
  }
}
