package ru.hse.antiplag.filestorageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hse.antiplag.filestorageservice.domain.FileEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for working with FileEntity entities.
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
  Optional<FileEntity> findByHash(String hash);
}
