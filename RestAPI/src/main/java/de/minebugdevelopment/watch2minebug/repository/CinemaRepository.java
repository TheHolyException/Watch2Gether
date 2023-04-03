package de.minebugdevelopment.watch2minebug.repository;

import de.minebugdevelopment.watch2minebug.entity.CinemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CinemaRepository extends JpaRepository<CinemaEntity, UUID> {

}
