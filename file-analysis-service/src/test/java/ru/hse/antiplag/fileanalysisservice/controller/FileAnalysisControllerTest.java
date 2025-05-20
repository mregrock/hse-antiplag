package ru.hse.antiplag.fileanalysisservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.antiplag.fileanalysisservice.dto.AnalysisResult;
import ru.hse.antiplag.fileanalysisservice.dto.TextStatistics;
import ru.hse.antiplag.fileanalysisservice.service.FileAnalysisService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link FileAnalysisController}.
 */
@WebMvcTest(FileAnalysisController.class)
public class FileAnalysisControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FileAnalysisService fileAnalysisService;

  /**
   * Test for analyzing a file successfully.
   *
   * @throws Exception if an error occurs during the mock MVC call.
   */
  @Test
  void analyzeFile_whenFileExists_shouldReturnAnalysisResult() throws Exception {
    String fileId = "test-file-id";
    TextStatistics expectedStats = new TextStatistics(10, 100, 1000);
    AnalysisResult expectedResult = new AnalysisResult(expectedStats, "path/to/cloud.png");

    when(fileAnalysisService.analyzeFile(anyString())).thenReturn(expectedResult);

    mockMvc.perform(get("/api/v1/analysis/{fileId}", fileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.textStatistics.paragraphCount").value(10))
        .andExpect(jsonPath("$.textStatistics.wordCount").value(100))
        .andExpect(jsonPath("$.textStatistics.characterCount").value(1000))
        .andExpect(jsonPath("$.wordCloudPath").value("path/to/cloud.png"));
  }
}
