package ru.hse.antiplag.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient instances used by the API Gateway.
 */
@Configuration
public class WebClientConfig {

  @Value("${services.file-analysis.base-url:http://localhost:9090}")
  private String fileAnalysisServiceBaseUrl;

  @Value("${services.file-storage.base-url:http://localhost:9001}")
  private String fileStorageServiceBaseUrl;

  /**
   * Creates a WebClient bean for interacting with the FileAnalysisService.
   *
   * @return a configured WebClient instance for FileAnalysisService.
   */
  @Bean
  public WebClient fileAnalysisServiceWebClient() {
    return WebClient.builder()
        .baseUrl(fileAnalysisServiceBaseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  /**
   * Creates a WebClient bean for interacting with the FileStorageService.
   *
   * @return a configured WebClient instance for FileStorageService.
   */
  @Bean
  public WebClient fileStorageServiceWebClient() {
    return WebClient.builder()
        .baseUrl(fileStorageServiceBaseUrl)
        .build();
  }
}
