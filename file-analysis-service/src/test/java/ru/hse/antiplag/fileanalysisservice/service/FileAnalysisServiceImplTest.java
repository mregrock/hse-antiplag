package ru.hse.antiplag.fileanalysisservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import ru.hse.antiplag.fileanalysisservice.dto.AnalysisResult;
import ru.hse.antiplag.fileanalysisservice.dto.TextStatistics;
import ru.hse.antiplag.fileanalysisservice.dto.WordCloudRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.hse.antiplag.fileanalysisservice.entity.AnalysisResultEntity;
import ru.hse.antiplag.fileanalysisservice.repository.AnalysisResultRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Component tests for {@link FileAnalysisServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
public class FileAnalysisServiceImplTest {

  private MockWebServer mockFileStorageService;
  private MockWebServer mockWordCloudService;

  @Mock
  private AnalysisResultRepository analysisResultRepository;

  @InjectMocks
  private FileAnalysisServiceImpl fileAnalysisService;

  private ObjectMapper objectMapper = new ObjectMapper();
  private WebClient fileStorageWebClient;
  private WebClient wordCloudWebClient;

  private static class MockFileStorageUploadResponse {
    public UUID id;
    public String fileName;
    public MockFileStorageUploadResponse(UUID id, String fileName) {
      this.id = id;
      this.fileName = fileName;
    }
  }

  @BeforeEach
  void setUp() throws IOException {
    mockFileStorageService = new MockWebServer();
    mockFileStorageService.start();
    String fileStorageBaseUrl = String.format("http://localhost:%s", mockFileStorageService.getPort());
    fileStorageWebClient = WebClient.builder().baseUrl(fileStorageBaseUrl).build();

    mockWordCloudService = new MockWebServer();
    mockWordCloudService.start();
    String wordCloudBaseUrl = String.format("http://localhost:%s", mockWordCloudService.getPort());
    wordCloudWebClient = WebClient.builder().baseUrl(wordCloudBaseUrl).build();

    fileAnalysisService = new FileAnalysisServiceImpl(fileStorageWebClient, wordCloudWebClient, analysisResultRepository);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockFileStorageService.shutdown();
    mockWordCloudService.shutdown();
  }

  @Test
  void resultInDbTest() {
    String fileId = "cached-aboba";
    AnalysisResultEntity cachedEntity = new AnalysisResultEntity(fileId, 1, 10, 100, "/path/to/kek.png");
    cachedEntity.setCreatedAt(LocalDateTime.now().minusDays(1));
    cachedEntity.setUpdatedAt(LocalDateTime.now().minusDays(1));

    when(analysisResultRepository.findByFileId(fileId)).thenReturn(Optional.of(cachedEntity));
    when(analysisResultRepository.save(any(AnalysisResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    AnalysisResult result = fileAnalysisService.analyzeFile(fileId);

    assertNotNull(result);
    assertEquals(1, result.getTextStatistics().getParagraphCount());
    assertEquals(10, result.getTextStatistics().getWordCount());
    assertEquals(100, result.getTextStatistics().getCharacterCount());
    assertEquals("/path/to/kek.png", result.getWordCloudPath());

    verify(analysisResultRepository).findByFileId(fileId);
    verify(analysisResultRepository).save(any(AnalysisResultEntity.class));

    assertEquals(0, mockFileStorageService.getRequestCount());
    assertEquals(0, mockWordCloudService.getRequestCount());
  }

  @Test
  void analyzeNewFileTest() throws Exception {
    String fileId = "new-aboba";
    String mockFileContent = "This is aboba content.";
    byte[] mockImageBytes = "new-dummy-kek-bytes".getBytes(StandardCharsets.UTF_8);
    UUID wordCloudFileId = UUID.randomUUID();

    when(analysisResultRepository.findByFileId(fileId)).thenReturn(Optional.empty());
    when(analysisResultRepository.save(any(AnalysisResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    mockFileStorageService.enqueue(new MockResponse().setBody(mockFileContent).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE));
    mockWordCloudService.enqueue(new MockResponse().setBody(new okio.Buffer().write(mockImageBytes)).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE));
    MockFileStorageUploadResponse mockFsUploadResponse = new MockFileStorageUploadResponse(wordCloudFileId, "wordcloud_" + fileId + ".png");
    mockFileStorageService.enqueue(new MockResponse().setBody(objectMapper.writeValueAsString(mockFsUploadResponse)).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setResponseCode(201));

    AnalysisResult result = fileAnalysisService.analyzeFile(fileId);

    assertNotNull(result);
    assertEquals(1, result.getTextStatistics().getParagraphCount());
    assertEquals(4, result.getTextStatistics().getWordCount());
    assertEquals(mockFileContent.length(), result.getTextStatistics().getCharacterCount());
    assertEquals("/api/v1/files/download/" + wordCloudFileId.toString(), result.getWordCloudPath());

    verify(analysisResultRepository).findByFileId(fileId);
    verify(analysisResultRepository).save(any(AnalysisResultEntity.class));
    assertEquals(2, mockFileStorageService.getRequestCount());
    assertEquals(1, mockWordCloudService.getRequestCount());
  }
  
  @Test
    void nullContentTest() {
        String fileId = "null-aboba-content";
        when(analysisResultRepository.findByFileId(fileId)).thenReturn(Optional.empty());
        mockFileStorageService.enqueue(new MockResponse().setResponseCode(404));

        AnalysisResult result = fileAnalysisService.analyzeFile(fileId);

        assertNotNull(result);
        assertNotNull(result.getTextStatistics());
        assertEquals(0, result.getTextStatistics().getParagraphCount());
        assertEquals(0, result.getTextStatistics().getWordCount());
        assertEquals(0, result.getTextStatistics().getCharacterCount());
        assertEquals("", result.getWordCloudPath());

        verify(analysisResultRepository).findByFileId(fileId);
        verify(analysisResultRepository, never()).save(any(AnalysisResultEntity.class));
        assertEquals(1, mockFileStorageService.getRequestCount());
        assertEquals(0, mockWordCloudService.getRequestCount());
    }

    @Test
    void emptyContentTest() {
        String fileId = "empty-aboba-file";
        when(analysisResultRepository.findByFileId(fileId)).thenReturn(Optional.empty());

        mockFileStorageService.enqueue(new MockResponse()
            .setBody(new okio.Buffer())
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE));

        AnalysisResult result = fileAnalysisService.analyzeFile(fileId);

        assertNotNull(result);
        assertEquals(0, result.getTextStatistics().getWordCount());
        assertEquals("", result.getWordCloudPath());

        verify(analysisResultRepository).findByFileId(fileId);
        verify(analysisResultRepository, never()).save(any(AnalysisResultEntity.class));
        assertEquals(1, mockFileStorageService.getRequestCount());
        assertEquals(0, mockWordCloudService.getRequestCount());
    }

  @Test
  void fullSuccessTest() throws Exception {
    String fileId = "aboba-test-id-full-success";
    String mockFileContent = "This is some aboba test text. Repetitive kek text for cloud.";
    byte[] mockImageBytes = "dummy-image-aboba-bytes".getBytes(StandardCharsets.UTF_8);
    UUID wordCloudFileId = UUID.randomUUID();

    when(analysisResultRepository.findByFileId(fileId)).thenReturn(Optional.empty());
    when(analysisResultRepository.save(any(AnalysisResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    mockFileStorageService.enqueue(new MockResponse().setBody(mockFileContent).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE));
    mockWordCloudService.enqueue(new MockResponse().setBody(new okio.Buffer().write(mockImageBytes)).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE));
    MockFileStorageUploadResponse mockFsUploadResponse = new MockFileStorageUploadResponse(wordCloudFileId, "wordcloud_aboba_" + fileId + ".png");
    mockFileStorageService.enqueue(new MockResponse().setBody(objectMapper.writeValueAsString(mockFsUploadResponse)).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setResponseCode(201));

    AnalysisResult result = fileAnalysisService.analyzeFile(fileId);

    assertNotNull(result);
    TextStatistics stats = result.getTextStatistics();
    assertEquals(1, stats.getParagraphCount());
    assertEquals(11, stats.getWordCount());

    verify(analysisResultRepository).findByFileId(fileId);
    verify(analysisResultRepository).save(any(AnalysisResultEntity.class));
    assertEquals(2, mockFileStorageService.getRequestCount());
    assertEquals(1, mockWordCloudService.getRequestCount());
  }
  
    @Test
    void wordCloudApiFailTest() throws InterruptedException {
        String fileId = "wordcloud-fail-aboba";
        String mockFileContent = "Some valid aboba text content.";

        when(analysisResultRepository.findByFileId(fileId)).thenReturn(Optional.empty());

        mockFileStorageService.enqueue(new MockResponse().setBody(mockFileContent).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE));
        mockWordCloudService.enqueue(new MockResponse().setResponseCode(500));

        AnalysisResult result = fileAnalysisService.analyzeFile(fileId);

        assertNotNull(result);
        assertEquals(1, result.getTextStatistics().getParagraphCount());
        assertEquals(5, result.getTextStatistics().getWordCount());
        assertEquals("", result.getWordCloudPath());

        verify(analysisResultRepository).findByFileId(fileId);
        verify(analysisResultRepository, never()).save(any(AnalysisResultEntity.class));
        assertEquals(1, mockFileStorageService.getRequestCount());
        assertEquals(1, mockWordCloudService.getRequestCount());
    }

    @Test
    void wordCloudUploadFailTest() throws InterruptedException {
        String fileId = "upload-fail-aboba";
        String mockFileContent = "Some aboba text for analysis.";
        byte[] mockImageBytes = "dummy-image-kek".getBytes(StandardCharsets.UTF_8);

        when(analysisResultRepository.findByFileId(fileId)).thenReturn(Optional.empty());

        mockFileStorageService.enqueue(new MockResponse().setBody(mockFileContent).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE));
        mockWordCloudService.enqueue(new MockResponse().setBody(new okio.Buffer().write(mockImageBytes)).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE));
        mockFileStorageService.enqueue(new MockResponse().setResponseCode(500));

        AnalysisResult result = fileAnalysisService.analyzeFile(fileId);

        assertNotNull(result);
        assertEquals(1, result.getTextStatistics().getParagraphCount());
        assertEquals(5, result.getTextStatistics().getWordCount());
        assertEquals("", result.getWordCloudPath());

        verify(analysisResultRepository).findByFileId(fileId);
        verify(analysisResultRepository, never()).save(any(AnalysisResultEntity.class));
        assertEquals(2, mockFileStorageService.getRequestCount());
        assertEquals(1, mockWordCloudService.getRequestCount());
    }
}
