package de.minebugdevelopment.watch2minebug.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "cinema")
public class CinemaEntity implements IEntity {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid = UUID.randomUUID();

    @Setter
    @ManyToOne(cascade = CascadeType.PERSIST)
    private UserEntity owner;

    @JoinTable(
            name = "_MTM_cinema_video",
            joinColumns = {
                    @JoinColumn(name = "cinemalink")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "videolink")
            }
    )
    @ManyToMany(fetch = FetchType.EAGER)
    @ElementCollection(targetClass = VideoEntity.class)
    private Set<VideoEntity> videolist = new HashSet<>();

    private String name;
}
