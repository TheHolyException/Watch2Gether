package de.minebugdevelopment.watch2minebug.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "userGroups")
public class UserGroupEntity implements IEntity {

	@Id
	@Type(type = "uuid-char")
	private UUID uuid = UUID.randomUUID();
	
	private String name;
	
	@JsonIgnore
	@ManyToMany(mappedBy = "usergroups")
	private Set<UserEntity> users;

	@JsonIgnore
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn
	private UserGroupEntity parent;

	@JsonIgnore
	@ElementCollection
	@CollectionTable(name = "usergroupPermissions")
	private Set<String> permissions;
	
}
