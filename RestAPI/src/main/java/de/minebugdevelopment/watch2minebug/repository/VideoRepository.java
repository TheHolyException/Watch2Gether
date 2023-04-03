package de.minebugdevelopment.watch2minebug.repository;

import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import de.minebugdevelopment.watch2minebug.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, UUID> {

    List<VideoEntity> findAllByUploader(UserEntity uploader);
}
