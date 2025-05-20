package ru.hse.antiplag.fileanalysisservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hse.antiplag.fileanalysisservice.entity.AnalysisResultEntity;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link AnalysisResultEntity} entity.
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, String> {

  /**
   * Finds an analysis result by its file ID.
   *
   * @param fileId the ID of the original file.
   * @return an {@link Optional} containing the analysis result if found, or empty otherwise.
   */
  Optional<AnalysisResultEntity> findByFileId(String fileId);
}
