package de.minebugdevelopment.watch2minebug.repository;

import de.minebugdevelopment.watch2minebug.entity.UserGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroupEntity, UUID> {
	
	Optional<UserGroupEntity> findUserGroupByUuid(UUID uuid);
	Optional<UserGroupEntity> findByName(String name);

}
