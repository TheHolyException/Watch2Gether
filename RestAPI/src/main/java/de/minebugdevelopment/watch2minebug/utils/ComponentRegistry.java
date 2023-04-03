package de.minebugdevelopment.watch2minebug.utils;

import de.minebugdevelopment.watch2minebug.Watch2Minebug;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class ComponentRegistry {
	
	private static final Map<String, JpaRepository> repositories = new HashMap<>();

	public static void init() {
		repositories.putAll(Watch2Minebug.getApplicationContext().getBeansOfType(JpaRepository.class));
	}

	private ComponentRegistry() {}

	public static Optional<JpaRepository> getRepository(Class<? extends JpaRepository> clazz) {
		for (Map.Entry<String, JpaRepository> entry : repositories.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(clazz.getSimpleName()))
				return Optional.of(entry.getValue());
		}
		return Optional.empty();
	}

	public static Optional<JpaRepository> getRepositoryByName(String name) {
		return Optional.of(repositories.get(name));
	}
}
