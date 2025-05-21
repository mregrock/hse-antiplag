package ru.hse.antiplag.apigateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.hse.antiplag.apigateway.dto.FileUploadResponse;
import ru.hse.antiplag.apigateway.dto.GatewayAnalysisResult;
import ru.hse.antiplag.apigateway.dto.GatewayTextStatistics;

/**
 * Tests for {@link GatewayController}.
 */
@WebFluxTest(GatewayController.class)
public class GatewayControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean(name = "fileAnalysisServiceWebClient")
  private WebClient fileAnalysisServiceWebClient;

  @MockBean(name = "fileStorageServiceWebClient")
  private WebClient fileStorageServiceWebClient;

  @SuppressWarnings("rawtypes")
  private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;
  @SuppressWarnings("rawtypes")
  private WebClient.RequestHeadersSpec requestHeadersSpecMock;
  private WebClient.ResponseSpec responseSpecMock;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    requestHeadersUriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    requestHeadersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
    responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

    WebClient.RequestHeadersSpec analysisServiceGetSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
    when(fileAnalysisServiceWebClient.get()).thenReturn(requestHeadersUriSpecMock);
    when(requestHeadersUriSpecMock.uri(anyString(), Mockito.eq("test-aboba-id"))).thenReturn(analysisServiceGetSpec);
    when(requestHeadersUriSpecMock.uri(anyString(), Mockito.eq("fail-aboba-id"))).thenReturn(analysisServiceGetSpec);
    when(analysisServiceGetSpec.retrieve()).thenReturn(responseSpecMock);

    WebClient.RequestBodyUriSpec requestBodyUriSpecMock = Mockito.mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestBodySpec requestBodySpecMock = Mockito.mock(WebClient.RequestBodySpec.class);
    WebClient.RequestHeadersSpec storageServicePostSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
    when(fileStorageServiceWebClient.post()).thenReturn(requestBodyUriSpecMock);
    when(requestBodyUriSpecMock.uri(Mockito.eq("/upload"))).thenReturn(requestBodySpecMock);
    when(requestBodySpecMock.contentType(any(MediaType.class))).thenReturn(requestBodySpecMock);
    when(requestBodySpecMock.body(any())).thenReturn(storageServicePostSpec);
    when(storageServicePostSpec.retrieve()).thenReturn(responseSpecMock);

    WebClient.RequestHeadersSpec storageServiceGetSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
    when(fileStorageServiceWebClient.get()).thenReturn(requestHeadersUriSpecMock);
    when(requestHeadersUriSpecMock.uri(anyString(), Mockito.eq("test-download-aboba"))).thenReturn(storageServiceGetSpec);
    when(requestHeadersUriSpecMock.uri(anyString(), Mockito.eq("fail-download-aboba"))).thenReturn(storageServiceGetSpec);
    when(storageServiceGetSpec.accept(any(MediaType.class))).thenReturn(storageServiceGetSpec);
    when(storageServiceGetSpec.retrieve()).thenReturn(responseSpecMock);
  }

  @Test
  void analyzeFile_whenAnalysisSucceeds_shouldReturnResult() {
    GatewayTextStatistics stats = new GatewayTextStatistics(1, 10, 100);
    GatewayAnalysisResult mockResult = new GatewayAnalysisResult(stats, "/path/to/cloud-aboba.png");

    when(responseSpecMock.bodyToMono(GatewayAnalysisResult.class)).thenReturn(Mono.just(mockResult));

    webTestClient.get().uri("/api/gateway/analyze/test-aboba-id")
        .exchange()
        .expectStatus().isOk()
        .expectBody(GatewayAnalysisResult.class)
        .isEqualTo(mockResult);
  }

  @Test
  void analyzeFile_whenAnalysisServiceFails_shouldReturnErrorStatus() {
    when(responseSpecMock.bodyToMono(GatewayAnalysisResult.class)).thenReturn(Mono.error(new RuntimeException("Service Unavailable")));

    webTestClient.get().uri("/api/gateway/analyze/fail-aboba-id")
        .exchange()
        .expectStatus().is5xxServerError();
  }

  @Test
  void uploadFile_whenStorageSucceeds_shouldReturnUploadResponse() {
    UUID fileUuid = UUID.randomUUID();
    FileUploadResponse mockResponse = new FileUploadResponse(fileUuid, "test-file.txt");

    when(responseSpecMock.bodyToMono(FileUploadResponse.class)).thenReturn(Mono.just(mockResponse));

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", new ByteArrayResource("dummy data".getBytes()));

    webTestClient.post().uri("/api/gateway/upload")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().isOk()
        .expectBody(FileUploadResponse.class)
        .isEqualTo(mockResponse);
  }

  @Test
  void uploadFile_whenStorageFails_shouldReturnErrorStatus() {
    when(responseSpecMock.bodyToMono(FileUploadResponse.class)).thenReturn(Mono.error(new RuntimeException("Storage Service Error")));

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", new ByteArrayResource("dummy fail data".getBytes()));

    webTestClient.post().uri("/api/gateway/upload")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().is5xxServerError();
  }

  @Test
  void downloadFile_whenStorageSucceeds_shouldReturnFileResource() {
    String fileId = "test-download-aboba";
    byte[] fileContent = "Hello Aboba!".getBytes();
    Resource mockResource = new ByteArrayResource(fileContent);

    when(responseSpecMock.bodyToMono(Resource.class)).thenReturn(Mono.just(mockResource));

    webTestClient.get().uri("/api/gateway/download/" + fileId)
        .accept(MediaType.APPLICATION_OCTET_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
        .expectBody(byte[].class)
        .isEqualTo(fileContent);
  }

  @Test
  void downloadFile_whenStorageFails_shouldReturnErrorStatus() {
    String fileId = "fail-download-aboba";
    when(responseSpecMock.bodyToMono(Resource.class)).thenReturn(Mono.error(new RuntimeException("Storage Not Found")));

    webTestClient.get().uri("/api/gateway/download/" + fileId)
        .exchange()
        .expectStatus().is5xxServerError();
  }
}
