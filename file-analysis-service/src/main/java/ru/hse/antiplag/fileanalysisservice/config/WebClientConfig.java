package ru.hse.antiplag.fileanalysisservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient instances.
 */
@Configuration
public class WebClientConfig {

  @Value("${file.storage.service.url:http://localhost:8081/api/v1/files}")
  private String fileStorageServiceUrl;

  @Value("${wordcloud.service.url:https://quickchart.io/wordcloud}")
  private String wordCloudServiceUrl;

  /**
   * Creates a WebClient bean for interacting with the File Storage Service.
   *
   * @return configured WebClient instance for file storage.
   */
  @Bean
  @Qualifier("fileStorageWebClient")
  public WebClient fileStorageWebClient() {
    return WebClient.builder()
        .baseUrl(fileStorageServiceUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  /**
   * Creates a WebClient bean for interacting with the Word Cloud Service.
   *
   * @return configured WebClient instance for word cloud generation.
   */
  @Bean
  @Qualifier("wordCloudWebClient")
  public WebClient wordCloudWebClient() {
    return WebClient.builder()
        .baseUrl(wordCloudServiceUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.IMAGE_PNG_VALUE)
        .build();
  }
}
